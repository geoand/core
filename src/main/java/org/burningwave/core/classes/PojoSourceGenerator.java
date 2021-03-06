package org.burningwave.core.classes;

import static org.burningwave.core.assembler.StaticComponentContainer.Classes;
import static org.burningwave.core.assembler.StaticComponentContainer.Strings;
import static org.burningwave.core.assembler.StaticComponentContainer.Throwables;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import org.burningwave.core.function.QuadConsumer;
import org.burningwave.core.function.TriConsumer;

public class PojoSourceGenerator {
	public static int ALL_OPTIONS_DISABLED = 0b00000000;
	public static int BUILDING_METHODS_CREATION_ENABLED = 0b00000001;
	public static int USE_OF_FULLY_QUALIFIED_CLASS_NAMES_ENABLED = 0b00000010;
	
	private BiConsumer<Map<String, VariableSourceGenerator>, ClassSourceGenerator> fieldsBuilder;
	private TriConsumer<FunctionSourceGenerator, Method, Integer> setterMethodsBodyBuilder;
	private TriConsumer<FunctionSourceGenerator, Method, Integer> getterMethodsBodyBuilder;
	private QuadConsumer<UnitSourceGenerator, Class<?>, Collection<Class<?>>, Integer> extraElementsBuilder;
	
	private PojoSourceGenerator(
		BiConsumer<Map<String, VariableSourceGenerator>, ClassSourceGenerator> fieldsBuilder,
		TriConsumer<FunctionSourceGenerator, Method, Integer> setterMethodsBodyBuilder,
		TriConsumer<FunctionSourceGenerator, Method, Integer> getterMethodsBodyBuilder,
		QuadConsumer<UnitSourceGenerator, Class<?>, Collection<Class<?>>, Integer> extraElementsBuilder
	) {
		this.fieldsBuilder = fieldsBuilder;
		this.setterMethodsBodyBuilder = setterMethodsBodyBuilder;
		this.getterMethodsBodyBuilder = getterMethodsBodyBuilder;
		this.extraElementsBuilder = extraElementsBuilder;
	}
	
	public static PojoSourceGenerator createDefault() {
		return new PojoSourceGenerator(
			(fieldsMap, cls) -> {
				fieldsMap.entrySet().forEach(entry -> {
					cls.addField(entry.getValue().addModifier(Modifier.PRIVATE));
				});
			}, (methodSG, method, options) -> {
				String fieldName = Strings.lowerCaseFirstCharacter(method.getName().replaceFirst("set", ""));
				methodSG.addBodyCodeRow("this." + fieldName + " = " + fieldName + ";");
			}, (methodSG, method, options) -> {
				String prefix = method.getName().startsWith("get")? "get" : "is";
				String fieldName = Strings.lowerCaseFirstCharacter(method.getName().replaceFirst(prefix, ""));
				methodSG.addBodyCodeRow("return this." + fieldName + ";");
			}, null
		);
	}
	
	public PojoSourceGenerator setFieldsBuilder(BiConsumer<Map<String, VariableSourceGenerator>, ClassSourceGenerator> fieldsBuilder) {
		this.fieldsBuilder = fieldsBuilder;
		return this;
	}

	public PojoSourceGenerator setSetterMethodsBodyBuilder(TriConsumer<FunctionSourceGenerator, Method, Integer> setterMethodsBodyBuilder) {
		this.setterMethodsBodyBuilder = setterMethodsBodyBuilder;
		return this;
	}

	public PojoSourceGenerator setGetterMethodsBodyBuilder(TriConsumer<FunctionSourceGenerator, Method, Integer> getterMethodsBodyBuilder) {
		this.getterMethodsBodyBuilder = getterMethodsBodyBuilder;
		return this;
	}

	public PojoSourceGenerator setExtraElementsBuilder(
		QuadConsumer<UnitSourceGenerator, Class<?>, Collection<Class<?>>, Integer> extraElementsBuilder
	) {
		this.extraElementsBuilder = extraElementsBuilder;
		return this;
	}
	
	public UnitSourceGenerator create(String className, int options, Class<?>... superClasses) {
		if (className.contains("$")) {
			throw Throwables.toRuntimeException(className + " Pojo could not be a inner class");
		}
		String packageName = Classes.retrievePackageName(className);
		String classSimpleName = Classes.retrieveSimpleName(className);
		ClassSourceGenerator cls = ClassSourceGenerator.create(
			TypeDeclarationSourceGenerator.create(classSimpleName)
		).addModifier(
			Modifier.PUBLIC
		);
		Class<?> superClass = null;
		Collection<Class<?>> interfaces = new LinkedHashSet<>();
		for (Class<?> iteratedSuperClass : superClasses) {
			if (iteratedSuperClass.isInterface()) {
				cls.addConcretizedType(createTypeDeclaration((options & USE_OF_FULLY_QUALIFIED_CLASS_NAMES_ENABLED) != 0, iteratedSuperClass));
				interfaces.add(iteratedSuperClass);
			} else if (superClass == null) {
				cls.expands(createTypeDeclaration(isUseFullyQualifiedClassNamesEnabled(options), iteratedSuperClass));
				superClass = iteratedSuperClass;
			} else {
				throw Throwables.toRuntimeException(className + " Pojo could not extends more than one class");
			}
		}
		if (superClass != null) {
			String superClassPackage = Optional.ofNullable(superClass.getPackage()).map(pckg -> pckg.getName()).orElseGet(() -> "");
			Predicate<Executable> modifierTester = 
				Strings.areEquals(packageName, superClassPackage) ?
					executable ->
						!Modifier.isPrivate(executable.getModifiers()) :
					executable ->
						Modifier.isPublic(executable.getModifiers()) ||
						Modifier.isProtected(executable.getModifiers());						
			for (Constructor<?> constructor : Classes.getDeclaredConstructors(superClass, constructor -> 
				modifierTester.test(constructor))
			) {
				Integer modifiers = constructor.getModifiers();
				if (isBuildingMethodsCreationEnabled(options)) {
					if (Modifier.isPublic(modifiers)) {
						modifiers ^= Modifier.PUBLIC;
					}
				}
				cls.addConstructor(
					create(
						classSimpleName, constructor, modifiers, (funct, params) ->
						funct.addBodyCodeRow("super(" + String.join(", ", params) + ");"),
						isUseFullyQualifiedClassNamesEnabled(options)
					)
				);
				if (isBuildingMethodsCreationEnabled(options)) {
					cls.addMethod(
						create(
							"create", constructor, modifiers, (funct, params) ->
								funct.addBodyCodeRow("return new " + classSimpleName + "(" + String.join(", ", params) + ");"),
							isUseFullyQualifiedClassNamesEnabled(options)
						).addModifier(Modifier.STATIC | Modifier.PUBLIC).setReturnType(classSimpleName)
					);
				}
			}
		}
		Map<String, VariableSourceGenerator> fieldsMap = new HashMap<>();
		for (Class<?> interf : interfaces) {
			for (Method method : Classes.getDeclaredMethods(interf, method -> 
				method.getName().startsWith("set") || method.getName().startsWith("get") || method.getName().startsWith("is")
			)) {
				Integer modifiers = method.getModifiers();
				if (Modifier.isAbstract(modifiers)) {
					modifiers ^= Modifier.ABSTRACT;
				}
				FunctionSourceGenerator methodSG = FunctionSourceGenerator.create(method.getName()).addModifier(modifiers);
				methodSG.setReturnType(createTypeDeclaration(isUseFullyQualifiedClassNamesEnabled(options), method.getReturnType()));
				if (method.getName().startsWith("set")) {
					String fieldName = Strings.lowerCaseFirstCharacter(method.getName().replaceFirst("set", ""));
					Class<?> paramType = method.getParameters()[0].getType();
					fieldsMap.put(fieldName, VariableSourceGenerator.create(createTypeDeclaration(isUseFullyQualifiedClassNamesEnabled(options), paramType), fieldName));
					methodSG.addParameter(VariableSourceGenerator.create(createTypeDeclaration(isUseFullyQualifiedClassNamesEnabled(options), paramType), fieldName));
					if (setterMethodsBodyBuilder != null) {
						setterMethodsBodyBuilder.accept(methodSG, method, options);
					}
				} else if (method.getName().startsWith("get") || method.getName().startsWith("is")) {
					String prefix = method.getName().startsWith("get")? "get" : "is";
					String fieldName = Strings.lowerCaseFirstCharacter(method.getName().replaceFirst(prefix, ""));
					fieldsMap.put(fieldName, VariableSourceGenerator.create(createTypeDeclaration(isUseFullyQualifiedClassNamesEnabled(options), method.getReturnType()), fieldName));
					if (getterMethodsBodyBuilder != null) {
						getterMethodsBodyBuilder.accept(methodSG, method, options);
					}
				}
				cls.addMethod(methodSG);
			}
			if (fieldsBuilder != null) {
				fieldsBuilder.accept(fieldsMap, cls);
			}
		}
		UnitSourceGenerator unit = UnitSourceGenerator.create(packageName).addClass(cls);
		if (extraElementsBuilder != null) {
			extraElementsBuilder.accept(unit, superClass, interfaces, options);
		}
		return unit;
	}
	
	public boolean isUseFullyQualifiedClassNamesEnabled(int options) {
		return (options & USE_OF_FULLY_QUALIFIED_CLASS_NAMES_ENABLED) != 0;
	}
	
	public boolean isBuildingMethodsCreationEnabled(int options) {
		return (options & BUILDING_METHODS_CREATION_ENABLED) != 0;
	}
	
	protected TypeDeclarationSourceGenerator createTypeDeclaration(boolean useFullyQualifiedNames,
			Class<?> cls) {
		if (useFullyQualifiedNames) {
			return TypeDeclarationSourceGenerator.create(cls.getName().replace("$", "."));
		} else {
			return TypeDeclarationSourceGenerator.create(cls);
		}
	};
	
	private FunctionSourceGenerator create(
		String functionName,
		Executable executable,
		Integer modifiers,
		BiConsumer<FunctionSourceGenerator, Collection<String>> bodyBuilder,
		boolean useFullyQualifiedNames
	) {
		FunctionSourceGenerator function = FunctionSourceGenerator.create(functionName);
		Collection<String> params = new ArrayList<>();
		for (Parameter paramType : executable.getParameters()) {
			function.addParameter(
				VariableSourceGenerator.create(createTypeDeclaration(useFullyQualifiedNames, paramType.getType()), paramType.getName())
			);
			params.add(paramType.getName());
		}
		bodyBuilder.accept(function, params);
		return function;
	}
}