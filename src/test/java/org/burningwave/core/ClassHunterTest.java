package org.burningwave.core;

import java.io.Closeable;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Date;

import org.burningwave.core.assembler.ComponentSupplier;
import org.burningwave.core.bean.Complex;
import org.burningwave.core.classes.CacheableSearchConfig;
import org.burningwave.core.classes.ClassCriteria;
import org.burningwave.core.classes.ClassHunter;
import org.burningwave.core.classes.ConstructorCriteria;
import org.burningwave.core.classes.MethodCriteria;
import org.burningwave.core.classes.SearchConfig;
import org.burningwave.core.io.PathHelper;
import org.junit.jupiter.api.Test;

public class ClassHunterTest extends BaseTest {
	
	@Test
	public void findAllTestOne() throws Exception {
		ComponentSupplier componentSupplier = getComponentSupplier();
		testNotEmpty(
			() -> componentSupplier.getClassHunter().findBy(
				SearchConfig.forPaths(
					componentSupplier.getPathHelper().getAbsolutePathOfResource("../../src/test/external-resources/libs-for-test.zip")
				)
			),
			(result) ->
				result.getClasses()
		);
	}

	@Test
	public void findAllSubtypeOfTestOne() {
		ComponentSupplier componentSupplier = getComponentSupplier();
		testNotEmpty(
			() -> componentSupplier.getClassHunter().findBy(
				SearchConfig.forPaths(
					//Search in the runtime Classpaths. Here you can add all absolute path you want:
					//both folders, zip and jar will be scanned recursively
					componentSupplier.getPathHelper().getPaths(PathHelper.MAIN_CLASS_PATHS, PathHelper.MAIN_CLASS_PATHS_EXTENSION)
				).by(
					ClassCriteria.create().byClasses((uploadedClasses, currentScannedClass) ->
						//[1]here you recall the uploaded class by "useClasses" method. In this case we're looking for all classes that extend com.github.burningwave.core.Item
						uploadedClasses.get(Complex.Data.Item.class).isAssignableFrom(currentScannedClass)
					).useClasses(
						//With this directive we ask the library to load one or more classes to be used for comparisons:
						//it serves to eliminate the problem that a class, loaded by different class loaders, 
						//turns out to be different for the comparison operators (eg. The isAssignableFrom method).
						//If you call this method, you must retrieve the uploaded class in all methods that support this feature like in the point[1]
						Complex.Data.Item.class
					)
				)
			),
			(result) -> result.getClasses()
		);
	}
	
	@Test
	public void findAllSubtypeOfTestTwo() {
		ComponentSupplier componentSupplier = getComponentSupplier();
		testNotEmpty(
			() -> componentSupplier.getClassHunter().findBy(
				SearchConfig.forPaths(
					componentSupplier.getPathHelper().getMainClassPaths()
				).by(
					ClassCriteria.create().byClasses((uploadedClasses, currentScannedClass) ->
						uploadedClasses.get(Serializable.class).isAssignableFrom(currentScannedClass)
					).useClasses(
						Serializable.class
					)
				)
			),
			(result) -> result.getClasses()
		);
	}

	
	@Test
	public void findAllSubtypeOfTestThree() {
		ComponentSupplier componentSupplier = getComponentSupplier();
		testNotEmpty(
			() -> componentSupplier.getClassHunter().findBy(
				SearchConfig.forPaths(
					componentSupplier.getPathHelper().getMainClassPaths()
				).by(
					ClassCriteria.create().byClasses((uploadedClasses, currentScannedClass) ->
						//[1]here you recall the uploaded class by "useClasses" method.
						//In this case we're looking for all classes that implements java.io.Closeable or java.io.Serializable
						uploadedClasses.get(Closeable.class).isAssignableFrom(currentScannedClass) ||
						uploadedClasses.get(Serializable.class).isAssignableFrom(currentScannedClass)
					).useClasses(
						//With this directive we ask the library to load one or more classes to be used for comparisons:
						//it serves to eliminate the problem that a class, loaded by different class loaders, 
						//turns out to be different for the comparison operators (eg. The isAssignableFrom method).
						//If you call this method, you must retrieve the uploaded class in all methods that support this feature like in the point[1]
						Closeable.class,
						Serializable.class
					)
				)
			),
			(result) -> result.getClasses()
		);
	}
	
	@Test
	public void findAllSubtypeOfTestFour() {
		ComponentSupplier componentSupplier = getComponentSupplier();
		testNotEmpty(
			() -> componentSupplier.getClassHunter().findBy(
				SearchConfig.forPaths(
					//Search in the runtime Classpaths. Here you can add all absolute path you want:
					//both folders, zip and jar will be scanned recursively
					componentSupplier.getPathHelper().getMainClassPaths()
				).by(
					ClassCriteria.create().byClasses((uploadedClasses, currentScannedClass) ->
						//[1]here you recall the uploaded class by "useClasses" method. In this case we're looking for all classes that extend java.util.AbstractList
						uploadedClasses.get(AbstractList.class).isAssignableFrom(currentScannedClass)
					).useClasses(
						//With this directive we ask the library to load one or more classes to be used for comparisons:
						//it serves to eliminate the problem that a class, loaded by different class loaders, 
						//turns out to be different for the comparison operators (eg. The isAssignableFrom method).
						//If you call this method, you must retrieve the uploaded class in all methods that support this feature like in the point[1]
						AbstractList.class
					)
				)
			),
			(result) -> result.getClasses()
		);
	}
	
	@Test
	public void findAllSubtypeOfTestFive() {
		ComponentSupplier componentSupplier = getComponentSupplier();
		testNotEmpty(
			() -> componentSupplier.getClassHunter().findBy(
				SearchConfig.forPaths(
					componentSupplier.getPathHelper().getMainClassPaths()
				).addPaths(
					componentSupplier.getPathHelper().getAbsolutePathOfResource("../../src")
				).by(
					ClassCriteria.create().byClasses((uploadedClasses, currentScannedClass) ->
						uploadedClasses.get(Serializable.class).isAssignableFrom(currentScannedClass)
					).useClasses(
						Serializable.class
					)
				)
			),
			(result) -> result.getClasses()
		);
	}
	
	@Test
	public void findAllSubtypeOfWithMethodsTestOne() {
		ComponentSupplier componentSupplier = getComponentSupplier();
		ClassHunter classHunter = componentSupplier.getClassHunter();
		testNotEmpty(
			() -> classHunter.findBy(
				SearchConfig.forPaths(
					componentSupplier.getPathHelper().getMainClassPaths()
				).by(
					ClassCriteria.create().byClasses((uploadedClasses, currentScannedClass) ->
						uploadedClasses.get(Closeable.class).isAssignableFrom(currentScannedClass) ||
						uploadedClasses.get(Serializable.class).isAssignableFrom(currentScannedClass)
					).and().byMembers(
						MethodCriteria.byScanUpTo(
							(uploadedClasses, initialClass, cls) -> cls.equals(uploadedClasses.get(Object.class))
						).parameterType(
							(array, idx) -> idx == 0 && array[idx].equals(int.class)
						).skip((classes, initialClass, examinedClass) -> 
							classes.get(Object.class) == examinedClass
						)
					).useClasses(
						Closeable.class,
						Serializable.class,
						Object.class
					)
				)
			),
			(result) ->
				result.getClasses()
		);
	}
	
	@Test
	public void findAllSubtypeOfWithMethodsTestTwo() {
		ComponentSupplier componentSupplier = getComponentSupplier();
		testNotEmpty(
			() -> componentSupplier.getClassHunter().findBy(
				SearchConfig.forPaths(
					componentSupplier.getPathHelper().getMainClassPaths()
				).by(
					ClassCriteria.create().byClasses((uploadedClasses, currentScannedClass) ->
						uploadedClasses.get(Closeable.class).isAssignableFrom(currentScannedClass) ||
						uploadedClasses.get(Serializable.class).isAssignableFrom(currentScannedClass)
					).and().byMembers(
						MethodCriteria.byScanUpTo(
							(uploadedClasses, initialClass, cls) ->
								cls.equals(uploadedClasses.get(Object.class))
						).parameterType(
							(array, idx) -> idx == 0 && array[idx].equals(int.class)
						).skip((classes, initialClass, examinedClass) -> 
							classes.get(Object.class) == examinedClass
						).result((foundMethods) ->
							foundMethods.size() > 3
						)
					).useClasses(
						Closeable.class,
						Serializable.class,
						Object.class
					)
				)
			),
			(result) -> result.getClasses()
		);
	}
	
	@Test
	public void findAllSubtypeOfWithMethodsTestThree() {
		ComponentSupplier componentSupplier = getComponentSupplier();
		PathHelper pathHelper = componentSupplier.getPathHelper();
		testNotEmpty(
			() -> componentSupplier.getClassHunter().findBy(
				SearchConfig.forPaths(
					componentSupplier.getPathHelper().getMainClassPaths(),
					Arrays.asList(pathHelper.getAbsolutePathOfResource("../../src/test/external-resources/libs-for-test.zip"))
				).by(
					ClassCriteria.create().byClasses((uploadedClasses, currentScannedClass) ->
						uploadedClasses.get(Closeable.class).isAssignableFrom(currentScannedClass)
					).and().byMembers(
						MethodCriteria.byScanUpTo(
							(uploadedClasses, initialClass, cls) ->
								cls.equals(initialClass)
						).parameterType(
							(uploadedClasses, array, idx) ->
								idx == 0 && array[idx].equals(uploadedClasses.get(BigDecimal.class))
						).skip((classes, initialClass, examinedClass) -> 
							classes.get(Object.class) == examinedClass
						)
					).useClasses(
						Closeable.class,
						BigDecimal.class,
						Object.class
					)
				).useSharedClassLoaderAsParent(
					true
				)
			),
			(result) ->
				result.getClasses()
		);
	}
	
	@Test
	public void findAllSubtypeOfWithMethodsTestFour() {
		ComponentSupplier componentSupplier = getComponentSupplier();
		MethodCriteria methodCriteria = MethodCriteria.byScanUpTo(
			(uploadedClasses, initialClass, cls) -> cls.equals(initialClass)
		).parameterType(
			(uploadedClasses, array, idx) ->
				idx == 0 && array[idx].equals(uploadedClasses.get(BigDecimal.class))
		).skip((classes, initialClass, examinedClass) -> 
			classes.get(Object.class) == examinedClass
		);
		
		testNotEmpty(
			() -> componentSupplier.getClassHunter().findBy(
				SearchConfig.forPaths(
					componentSupplier.getPathHelper().getAllPaths()
				).by(
					ClassCriteria.create().byClasses((uploadedClasses, currentScannedClass) ->
						uploadedClasses.get(Closeable.class).isAssignableFrom(currentScannedClass)
					).and().byMembers(
						methodCriteria
					).useClasses(
						Closeable.class,
						BigDecimal.class,
						Object.class
					)
				).useAsParentClassLoader(
					Thread.currentThread().getContextClassLoader()
				)
			),
			(result) ->
				result.getMembersBy(methodCriteria)
		);
	}
	
	@Test
	public void findAllSubtypeOfWithMethodsTestFive() {
		ComponentSupplier componentSupplier = getComponentSupplier();
		MethodCriteria methodCriteria = MethodCriteria.byScanUpTo(
			(uploadedClasses, initialClass, cls) -> cls.equals(initialClass)
		).parameterType(
			(uploadedClasses, array, idx) ->
				idx == 0 && array[idx].equals(uploadedClasses.get(BigDecimal.class))
		).skip((classes, initialClass, examinedClass) -> 
			classes.get(Object.class) == examinedClass
		);
		
		testNotEmpty(
			() -> componentSupplier.getClassHunter().findBy(
				SearchConfig.forPaths(
					componentSupplier.getPathHelper().getAllPaths()
				).by(
					ClassCriteria.create().byClasses((uploadedClasses, currentScannedClass) ->
						uploadedClasses.get(Object.class).isAssignableFrom(currentScannedClass)
					).and().byMembers(
						methodCriteria
					).useClasses(
						BigDecimal.class,
						Object.class
					)
				).useAsParentClassLoader(
					Thread.currentThread().getContextClassLoader()
				)
			),
			(result) -> result.getMembersBy(methodCriteria)
		);
	}
	
	@Test
	public void findAllSubtypeOfWithMethodsTestSix() {
		ComponentSupplier componentSupplier = getComponentSupplier();
		MethodCriteria methodCriteria = MethodCriteria.forName(
			(methodName) -> methodName.startsWith("set")
		).and().parameterType(
			(uploadedClasses, array, idx) ->
				idx == 0 && array[idx].equals(uploadedClasses.get(Date.class))
		).skip((classes, initialClass, examinedClass) -> 
			classes.get(Object.class) == examinedClass
		);
		
		testNotEmpty(
			() -> componentSupplier.getClassHunter().findBy(
				SearchConfig.forPaths(
					componentSupplier.getPathHelper().getMainClassPaths()
				).by(
					ClassCriteria.create().byMembers(
						methodCriteria
					).useClasses(
						Date.class,
						Object.class
					)
				).useAsParentClassLoader(
					Thread.currentThread().getContextClassLoader()
				)
			),
			(result) -> result.getMembersBy(methodCriteria)
		);
	}
	
	@Test
	public void findAllSubtypeOfWithMethodsTestSeven() {
		ComponentSupplier componentSupplier = getComponentSupplier();
		MethodCriteria methodCriteria_01 = MethodCriteria.byScanUpTo(
			(uploadedClasses, initialClass, cls) -> cls.equals(initialClass)
		).parameterType(
			(uploadedClasses, array, idx) ->
				idx == 0 && array[idx].equals(uploadedClasses.get(BigDecimal.class))
		).skip((classes, initialClass, examinedClass) -> 
			classes.get(Object.class) == examinedClass
		);
		
		MethodCriteria methodCriteria_02 = MethodCriteria.forName(
			(methodName) -> methodName.startsWith("set")
		).and().parameterType(
			(uploadedClasses, array, idx) ->
				idx == 0 && array[idx].equals(uploadedClasses.get(Date.class))
		).skip((classes, initialClass, examinedClass) -> 
			classes.get(Object.class) == examinedClass
		);
		
		testNotEmpty(
			() -> componentSupplier.getClassHunter().findBy(
				SearchConfig.forPaths(
					componentSupplier.getPathHelper().getAllPaths()
				).by(
					ClassCriteria.create().byClasses((uploadedClasses, currentScannedClass) ->
						uploadedClasses.get(Object.class).isAssignableFrom(currentScannedClass)
					).and().byMembers(
						methodCriteria_01.or(methodCriteria_02)
					).useClasses(
						BigDecimal.class,
						Date.class,
						Object.class
					)
				)
			),
			(result) -> result.getMembersBy(methodCriteria_01)
		);
	}
	
	@Test
	public void findAllSubtypeOfWithConstructorTestOne() {
		ComponentSupplier componentSupplier = getComponentSupplier();
		PathHelper pathHelper = componentSupplier.getPathHelper();
		ConstructorCriteria constructorCriteria = ConstructorCriteria.create().parameterType(
			(uploadedClasses, array, idx) ->
				idx == 0 && array[idx].equals(uploadedClasses.get(Date.class))
		).skip((classes, initialClass, examinedClass) -> 
			classes.get(Object.class) == examinedClass
		);
		
		testNotEmpty(
			() -> componentSupplier.getClassHunter().findBy(
				SearchConfig.forPaths(
					componentSupplier.getPathHelper().getMainClassPaths(),
					Arrays.asList(pathHelper.getAbsolutePathOfResource("../../src/test/external-resources/libs-for-test.zip"))
				).by(
					ClassCriteria.create().byMembers(
						constructorCriteria
					).useClasses(
						Date.class,
						Object.class
					)
				).useAsParentClassLoader(
					Thread.currentThread().getContextClassLoader()
				)
			),
			(result) ->
				result.getMembersBy(constructorCriteria)
		);
	}
	
	@Test
	public void findAllSubtypeOfWithMethodsByAsyncModeTestOne() {
		ComponentSupplier componentSupplier = getComponentSupplier();
		
		MethodCriteria methodCriteria = MethodCriteria.forName(
			(methodName) -> methodName.startsWith("set")
		).and().parameterType(
			(uploadedClasses, array, idx) ->
				idx == 0 && array[idx].equals(uploadedClasses.get(Date.class))
		).skip((classes, initialClass, examinedClass) -> 
			classes.get(Object.class) == examinedClass
		);
		
		testNotEmpty(
			() -> componentSupplier.getClassHunter().findBy(
				SearchConfig.forPaths(
					componentSupplier.getPathHelper().getAllPaths()
				).by(
					ClassCriteria.create().byMembers(
						methodCriteria
					).useClasses(
						Date.class,
						Object.class
					)
				).useAsParentClassLoader(
					Thread.currentThread().getContextClassLoader()
				).waitForSearchEnding(
					false
				)	
			),
			(result) -> {
				result.waitForSearchEnding();
				return result.getMembersBy(methodCriteria);
			}
		);
	}
	
	@Test
	public void cacheTestOne() {
		ComponentSupplier componentSupplier = getComponentSupplier();
		CacheableSearchConfig searchConfig = SearchConfig.forPaths(
			componentSupplier.getPathHelper().getMainClassPaths()
		);
		testNotEmpty(
			() -> componentSupplier.getClassHunter().findBy(searchConfig),
			(result) -> result.getClasses()
		);
		searchConfig.by(
			ClassCriteria.create().byClasses((uploadedClasses, currentScannedClass) -> 
				uploadedClasses.get(Closeable.class).isAssignableFrom(currentScannedClass)
			).useClasses(
				Closeable.class
			)
		);
		testNotEmpty(
			() -> componentSupplier.getClassHunter().findBy(searchConfig),
			(result) -> result.getClasses()
		);
	}
	
	@Test
	public void cacheTestTwo() {
		ComponentSupplier componentSupplier = getComponentSupplier();
		CacheableSearchConfig searchConfig = SearchConfig.forPaths(
			componentSupplier.getPathHelper().getMainClassPaths()
		);
		testNotEmpty(
			() -> componentSupplier.getClassHunter().loadInCache(searchConfig).find(),
			(result) -> result.getClasses()
		);
		searchConfig.by(
			ClassCriteria.create().byClasses((uploadedClasses, currentScannedClass) -> 
				uploadedClasses.get(Closeable.class).isAssignableFrom(currentScannedClass)
			).useClasses(
				Closeable.class
			)
		);
		testNotEmpty(
			() -> componentSupplier.getClassHunter().findBy(searchConfig),
			(result) -> result.getClasses()
		);
	}
	
	@Test
	public void findAllBurningWaveClassesByIsolatedClassLoader() {
		ComponentSupplier componentSupplier = getComponentSupplier();
		testNotEmpty(
			() -> componentSupplier.getClassHunter().findBy(
				SearchConfig.forPaths(
					componentSupplier.getPathHelper().getPath((path) ->
					path.endsWith("target/classes"))
				).by(
					ClassCriteria.create().allThat((currentScannedClass) -> 
						currentScannedClass.getPackage() != null &&
						currentScannedClass.getPackage().getName().startsWith("org.burningwave")
					)
				)
			),
			(result) ->
				result.getClasses(),
			false
		);
	}
	
	@Test
	public void findAllAnnotatedMethods() {
		ComponentSupplier componentSupplier = getComponentSupplier();
		testNotEmpty(
			() -> componentSupplier.getClassHunter().findBy(
				SearchConfig.forPaths(
						componentSupplier.getPathHelper().getMainClassPaths()
				).by(
					ClassCriteria.create().allThat((cls) -> {
						return cls.getAnnotations() != null && cls.getAnnotations().length > 0;
					}).or().byMembers(
						MethodCriteria.byScanUpTo((lastClassInHierarchy, currentScannedClass) -> {
							return lastClassInHierarchy.equals(currentScannedClass);
						}).allThat((method) -> {
							return method.getAnnotations() != null && method.getAnnotations().length > 0;
						})
					)
				)
			),
			(result) -> result.getClasses()
		);
	}
	
	@Test
	public void findAllTestOneByIsolatedClassLoader() throws Exception {
		ComponentSupplier componentSupplier = getComponentSupplier();
		testNotEmpty(
			() -> componentSupplier.getClassHunter().findBy(
				SearchConfig.forPaths(
					componentSupplier.getPathHelper().getAbsolutePathOfResource("../../src/test/external-resources/libs-for-test.zip")
				)
			),
			(result) ->
				result.getClasses()
		);
	}

	@Test
	public void findAllSubtypeOfTestOneByIsolatedClassLoader() {
		ComponentSupplier componentSupplier = getComponentSupplier();
		testNotEmpty(
			() -> componentSupplier.getClassHunter().findBy(
				SearchConfig.forPaths(
					//Search in the runtime Classpaths. Here you can add all absolute path you want:
					//both folders, zip and jar will be scanned recursively
					componentSupplier.getPathHelper().getMainClassPaths()
				).by(
					ClassCriteria.create().byClasses((uploadedClasses, currentScannedClass) ->
						//[1]here you recall the uploaded class by "useClasses" method. In this case we're looking for all classes that extend com.github.burningwave.core.Item
						uploadedClasses.get(Complex.Data.Item.class).isAssignableFrom(currentScannedClass)
					).useClasses(
						//With this directive we ask the library to load one or more classes to be used for comparisons:
						//it serves to eliminate the problem that a class, loaded by different class loaders, 
						//turns out to be different for the comparison operators (eg. The isAssignableFrom method).
						//If you call this method, you must retrieve the uploaded class in all methods that support this feature like in the point[1]
						Complex.Data.Item.class
					)
				).isolateClassLoader()
			),
			(result) ->
				result.getClasses()
		);
	}
	
	@Test
	public void findAllSubtypeOfTestTwoByIsolatedClassLoader() {
		ComponentSupplier componentSupplier = getComponentSupplier();
		testNotEmpty(
			() -> componentSupplier.getClassHunter().findBy(
				SearchConfig.forPaths(
					componentSupplier.getPathHelper().getMainClassPaths()
				).by(
					ClassCriteria.create().byClasses((uploadedClasses, currentScannedClass) ->
						uploadedClasses.get(Serializable.class).isAssignableFrom(currentScannedClass)
					).useClasses(
						Serializable.class
					)
				).isolateClassLoader()
			),
			(result) ->
				result.getClasses()
		);
	}

	
	@Test
	public void findAllSubtypeOfTestThreeByIsolatedClassLoader() {
		ComponentSupplier componentSupplier = getComponentSupplier();
		testNotEmpty(
			() -> componentSupplier.getClassHunter().findBy(
				SearchConfig.forPaths(
					componentSupplier.getPathHelper().getMainClassPaths()
				).by(
					ClassCriteria.create().byClasses((uploadedClasses, currentScannedClass) ->
						//[1]here you recall the uploaded class by "useClasses" method.
						//In this case we're looking for all classes that implements java.io.Closeable or java.io.Serializable
						uploadedClasses.get(Closeable.class).isAssignableFrom(currentScannedClass) ||
						uploadedClasses.get(Serializable.class).isAssignableFrom(currentScannedClass)
					).useClasses(
						//With this directive we ask the library to load one or more classes to be used for comparisons:
						//it serves to eliminate the problem that a class, loaded by different class loaders, 
						//turns out to be different for the comparison operators (eg. The isAssignableFrom method).
						//If you call this method, you must retrieve the uploaded class in all methods that support this feature like in the point[1]
						Closeable.class,
						Serializable.class
					)
				).isolateClassLoader()
			),
			(result) ->
				result.getClasses()
		);
	}
	
	@Test
	public void findAllSubtypeOfTestFourByIsolatedClassLoader() {
		ComponentSupplier componentSupplier = getComponentSupplier();
		testNotEmpty(
			() -> componentSupplier.getClassHunter().findBy(
				SearchConfig.forPaths(
					//Search in the runtime Classpaths. Here you can add all absolute path you want:
					//both folders, zip and jar will be scanned recursively
					componentSupplier.getPathHelper().getMainClassPaths()
				).by(
					ClassCriteria.create().byClasses((uploadedClasses, currentScannedClass) ->
						//[1]here you recall the uploaded class by "useClasses" method. In this case we're looking for all classes that extend java.util.AbstractList
						uploadedClasses.get(AbstractList.class).isAssignableFrom(currentScannedClass)
					).useClasses(
						//With this directive we ask the library to load one or more classes to be used for comparisons:
						//it serves to eliminate the problem that a class, loaded by different class loaders, 
						//turns out to be different for the comparison operators (eg. The isAssignableFrom method).
						//If you call this method, you must retrieve the uploaded class in all methods that support this feature like in the point[1]
						AbstractList.class
					)
				).isolateClassLoader()
			),
			(result) -> result.getClasses()
		);
	}
	
	@Test
	public void findAllSubtypeOfWithMethodsTestOneByIsolatedClassLoader() {
		ComponentSupplier componentSupplier = getComponentSupplier();
		testNotEmpty(
			() -> componentSupplier.getClassHunter().findBy(
				SearchConfig.forPaths(
					componentSupplier.getPathHelper().getMainClassPaths()
				).by(
					ClassCriteria.create().byClasses((uploadedClasses, currentScannedClass) ->
						uploadedClasses.get(Closeable.class).isAssignableFrom(currentScannedClass) ||
						uploadedClasses.get(Serializable.class).isAssignableFrom(currentScannedClass)
					).and().byMembers(
						MethodCriteria.byScanUpTo(
							(uploadedClasses, initialClass, cls) -> cls.equals(uploadedClasses.get(Object.class))
						).parameterType(
							(array, idx) -> idx == 0 && array[idx].equals(int.class)
						).skip((classes, initialClass, examinedClass) -> 
							classes.get(Object.class) == examinedClass
						)
					).useClasses(
						Closeable.class,
						Serializable.class,
						Object.class
					)
				).isolateClassLoader()
			),
			(result) ->
				result.getClasses()
		);
	}
	
	@Test
	public void findAllSubtypeOfWithMethodsTestTwoByIsolatedClassLoader() {
		ComponentSupplier componentSupplier = getComponentSupplier();
		testNotEmpty(
			() -> componentSupplier.getClassHunter().findBy(
				SearchConfig.forPaths(
					componentSupplier.getPathHelper().getMainClassPaths()
				).by(
					ClassCriteria.create().byClasses((uploadedClasses, currentScannedClass) ->
						uploadedClasses.get(Closeable.class).isAssignableFrom(currentScannedClass) ||
						uploadedClasses.get(Serializable.class).isAssignableFrom(currentScannedClass)
					).and().byMembers(
						MethodCriteria.byScanUpTo(
							(uploadedClasses, initialClass, cls) ->
								cls.equals(uploadedClasses.get(Object.class))
						).parameterType(
							(array, idx) -> idx == 0 && array[idx].equals(int.class)
						).skip((classes, initialClass, examinedClass) -> 
							classes.get(Object.class) == examinedClass
						).result((foundMethods) ->
							foundMethods.size() > 3
						)
					).useClasses(
						Closeable.class,
						Serializable.class,
						Object.class
					)
				).isolateClassLoader()
			),
			(result) ->
				result.getClasses()
		);
	}
	
	@Test
	public void findAllSubtypeOfWithMethodsTestThreeByIsolatedClassLoader() {
		ComponentSupplier componentSupplier = getComponentSupplier();
		PathHelper pathHelper = componentSupplier.getPathHelper();
		testNotEmpty(
			() -> componentSupplier.getClassHunter().findBy(
				SearchConfig.forPaths(
					componentSupplier.getPathHelper().getMainClassPaths(),
					Arrays.asList(pathHelper.getAbsolutePathOfResource("../../src/test/external-resources/libs-for-test.zip"))
				).by(
					ClassCriteria.create().byClasses((uploadedClasses, currentScannedClass) -> {
							return uploadedClasses.get(Closeable.class).isAssignableFrom(currentScannedClass);
						}
					).and().byMembers(
						MethodCriteria.byScanUpTo(
							(uploadedClasses, initialClass, cls) ->
								cls.equals(initialClass)
						).parameterType(
							(uploadedClasses, array, idx) ->
								idx == 0 && array[idx].equals(uploadedClasses.get(BigDecimal.class))
						).skip((classes, initialClass, examinedClass) -> 
							classes.get(Object.class) == examinedClass
						)
					).useClasses(
						Closeable.class,
						BigDecimal.class,
						Object.class
					)
				).useSharedClassLoaderAsParent(
					true
				).isolateClassLoader()
			),
			(result) ->
				result.getClasses(),
			false
		);
	}
	
	@Test
	public void findAllSubtypeOfWithMethodsTestFourByIsolatedClassLoader() {
		ComponentSupplier componentSupplier = getComponentSupplier();
		MethodCriteria methodCriteria = MethodCriteria.byScanUpTo(
			(uploadedClasses, initialClass, cls) -> cls.equals(initialClass)
		).parameterType(
			(uploadedClasses, array, idx) ->
				idx == 0 && array[idx].equals(uploadedClasses.get(BigDecimal.class))
		).skip((classes, initialClass, examinedClass) -> 
			classes.get(Object.class) == examinedClass
		);
		
		testNotEmpty(
			() -> componentSupplier.getClassHunter().findBy(
				SearchConfig.forPaths(
					componentSupplier.getPathHelper().getAllPaths()
				).by(
					ClassCriteria.create().byClasses((uploadedClasses, currentScannedClass) ->
						uploadedClasses.get(Closeable.class).isAssignableFrom(currentScannedClass)
					).and().byMembers(
						methodCriteria
					).useClasses(
						Closeable.class,
						BigDecimal.class,
						Object.class
					)
				).useAsParentClassLoader(
					Thread.currentThread().getContextClassLoader()
				).isolateClassLoader()
			),
			(result) ->
				result.getMembersBy(methodCriteria)
		);
	}
	
	@Test
	public void findAllSubtypeOfWithMethodsTestFiveByIsolatedClassLoader() {
		ComponentSupplier componentSupplier = getComponentSupplier();
		MethodCriteria methodCriteria = MethodCriteria.byScanUpTo(
			(uploadedClasses, initialClass, cls) -> cls.equals(initialClass)
		).parameterType(
			(uploadedClasses, array, idx) ->
				idx == 0 && array[idx].equals(uploadedClasses.get(BigDecimal.class))
		).skip((classes, initialClass, examinedClass) -> 
			classes.get(Object.class) == examinedClass
		);
		
		testNotEmpty(
			() -> componentSupplier.getClassHunter().findBy(
				SearchConfig.forPaths(
					componentSupplier.getPathHelper().getAllPaths()
				).by(
					ClassCriteria.create().byClasses((uploadedClasses, currentScannedClass) ->
						uploadedClasses.get(Object.class).isAssignableFrom(currentScannedClass)
					).and().byMembers(
						methodCriteria
					).useClasses(
						BigDecimal.class,
						Object.class
					)
				).useAsParentClassLoader(
					Thread.currentThread().getContextClassLoader()
				).isolateClassLoader()
			),
			(result) ->
				result.getMembersBy(methodCriteria)
		);
	}
	
	@Test
	public void findAllSubtypeOfWithMethodsTestSixByIsolatedClassLoader() {
		ComponentSupplier componentSupplier = getComponentSupplier();
		MethodCriteria methodCriteria = MethodCriteria.forName(
			(methodName) -> methodName.startsWith("set")
		).and().parameterType(
			(uploadedClasses, array, idx) ->
				idx == 0 && array[idx].equals(uploadedClasses.get(Date.class))
		).skip((classes, initialClass, examinedClass) -> 
			classes.get(Object.class) == examinedClass
		);
		
		testNotEmpty(
			() -> componentSupplier.getClassHunter().findBy(
				SearchConfig.forPaths(
					componentSupplier.getPathHelper().getMainClassPaths()
				).by(
					ClassCriteria.create().byMembers(
						methodCriteria
					).useClasses(
						Date.class,
						Object.class
					)
				).useAsParentClassLoader(
					Thread.currentThread().getContextClassLoader()
				).isolateClassLoader()
			),
			(result) ->
				result.getMembersBy(methodCriteria)
		);
	}
	
	@Test
	public void findAllSubtypeOfWithMethodsTestSevenByIsolatedClassLoader() {
		ComponentSupplier componentSupplier = getComponentSupplier();
		MethodCriteria methodCriteria_01 = MethodCriteria.byScanUpTo(
			(uploadedClasses, initialClass, cls) -> cls.equals(initialClass)
		).parameterType(
			(uploadedClasses, array, idx) ->
				idx == 0 && array[idx].equals(uploadedClasses.get(BigDecimal.class))
		).skip((classes, initialClass, examinedClass) -> 
			classes.get(Object.class) == examinedClass
		);
		
		MethodCriteria methodCriteria_02 = MethodCriteria.forName(
			(methodName) -> methodName.startsWith("set")
		).and().parameterType(
			(uploadedClasses, array, idx) ->
				idx == 0 && array[idx].equals(uploadedClasses.get(Date.class))
		).skip((classes, initialClass, examinedClass) -> 
			classes.get(Object.class) == examinedClass
		);
		
		testNotEmpty(
			() -> componentSupplier.getClassHunter().findBy(
				SearchConfig.forPaths(
					componentSupplier.getPathHelper().getAllPaths()
				).by(
					ClassCriteria.create().byClasses((uploadedClasses, currentScannedClass) ->
						uploadedClasses.get(Object.class).isAssignableFrom(currentScannedClass)
					).and().byMembers(
						methodCriteria_01.or(methodCriteria_02)
					).useClasses(
						BigDecimal.class,
						Date.class,
						Object.class
					)
				)
			),
			(result) -> result.getMembersBy(methodCriteria_01)
		);
	}
	
	@Test
	public void findAllSubtypeOfWithConstructorTestOneByIsolatedClassLoader() {
		ComponentSupplier componentSupplier = getComponentSupplier();
		PathHelper pathHelper = componentSupplier.getPathHelper();
		ConstructorCriteria constructorCriteria = ConstructorCriteria.create().parameterType(
			(uploadedClasses, array, idx) ->
				idx == 0 && array[idx].equals(uploadedClasses.get(Date.class))
		).skip((classes, initialClass, examinedClass) -> 
			classes.get(Object.class) == examinedClass
		);
		testNotEmpty(
			() -> componentSupplier.getClassHunter().findBy(
				SearchConfig.forPaths(
					componentSupplier.getPathHelper().getMainClassPaths(),
					Arrays.asList(pathHelper.getAbsolutePathOfResource("../../src/test/external-resources/libs-for-test.zip"))
				).by(
					ClassCriteria.create().byMembers(
						constructorCriteria
					).useClasses(
						Date.class,
						Object.class
					)
				).useAsParentClassLoader(
					Thread.currentThread().getContextClassLoader()
				).isolateClassLoader()
			),
			(result) ->
				result.getMembersBy(constructorCriteria)
		);
	}
	
	@Test
	public void findAllSubtypeOfWithMethodsByAsyncModeTestOneByIsolatedClassLoader() {
		ComponentSupplier componentSupplier = getComponentSupplier();
		
		MethodCriteria methodCriteria = MethodCriteria.forName(
			(methodName) -> methodName.startsWith("set")
		).and().parameterType(
			(uploadedClasses, array, idx) ->
				idx == 0 && array[idx].equals(uploadedClasses.get(Date.class))
		).skip((classes, initialClass, examinedClass) -> 
			classes.get(Object.class) == examinedClass
		);
		
		testNotEmpty(
			() -> componentSupplier.getClassHunter().findBy(
				SearchConfig.forPaths(
					componentSupplier.getPathHelper().getAllPaths()
				).by(
					ClassCriteria.create().byMembers(
						methodCriteria
					).useClasses(
						Date.class,
						Object.class
					)
				).useAsParentClassLoader(
					Thread.currentThread().getContextClassLoader()
				).waitForSearchEnding(
					false
				).isolateClassLoader()	
			),
			(result) -> {
				result.waitForSearchEnding();
				return result.getMembersBy(methodCriteria);
			}
		);
	}
	
	@Test
	public void cacheTestOneByIsolatedClassLoader() {
		ComponentSupplier componentSupplier = getComponentSupplier();
		CacheableSearchConfig searchConfig = SearchConfig.forPaths(
			componentSupplier.getPathHelper().getMainClassPaths()
		);
		testNotEmpty(
			() -> componentSupplier.getClassHunter().findBy(searchConfig),
			(result) -> result.getClasses()
		);
		searchConfig.by(
			ClassCriteria.create().byClasses((uploadedClasses, currentScannedClass) -> 
				uploadedClasses.get(Closeable.class).isAssignableFrom(currentScannedClass)
			).useClasses(
				Closeable.class
			)
		).isolateClassLoader();
		testNotEmpty(
			() ->
				componentSupplier.getClassHunter().findBy(searchConfig),
			(result) ->
				result.getClasses()
		);
	}
	
	@Test
	public void cacheTestTwoByIsolatedClassLoader() {
		ComponentSupplier componentSupplier = getComponentSupplier();
		CacheableSearchConfig searchConfig = SearchConfig.forPaths(
			componentSupplier.getPathHelper().getMainClassPaths()
		);
		testNotEmpty(
			() -> componentSupplier.getClassHunter().loadInCache(searchConfig).find(),
			(result) -> result.getClasses()
		);
		testNotEmpty(() -> 
			componentSupplier.getClassHunter().findBy(
				searchConfig.by(
					ClassCriteria.create().byClasses((uploadedClasses, currentScannedClass) -> 
						uploadedClasses.get(Closeable.class).isAssignableFrom(currentScannedClass)
					).useClasses(
						Closeable.class
					)
				).isolateClassLoader()
			),(result) ->
				result.getClasses()
		);
	}
	
	@Test
	public void findAllBurningWaveClasses() {
		ComponentSupplier componentSupplier = getComponentSupplier();
		testNotEmpty(
			() -> componentSupplier.getClassHunter().findBy(
				SearchConfig.forPaths(
					componentSupplier.getPathHelper().getPath((path) ->
					path.endsWith("target/classes"))
				).by(
					ClassCriteria.create().allThat((currentScannedClass) -> 
						currentScannedClass.getPackage() != null &&
						currentScannedClass.getPackage().getName().startsWith("org.burningwave")
					)
				).isolateClassLoader()
			),
			(result) ->
				result.getClasses(),
			false
		);
	}
	
//	@Test
//	public void findAllWithModuleByIsolatedClassLoader() {
//		ComponentSupplier componentSupplier = getComponentSupplier();
//		testNotEmpty(
//			() -> componentSupplier.getClassHunter().findBy(
//				SearchConfig.forPaths(
//					componentSupplier.getPathHelper().getMainClassPaths()
//				).by(
//					ClassCriteria.create().allThat((currentScannedClass) ->
//						currentScannedClass.getModule().getName() != null && 
//						currentScannedClass.getModule().getName().equals("jdk.xml.dom")
//					)
//				)
//			),
//			(result) ->
//				result.getItemsFound(),
//			false
//		);
//	}
	
	@Test
	public void findAllAnnotatedMethodsByIsolatedClassLoader() {
		ComponentSupplier componentSupplier = getComponentSupplier();
		testNotEmpty(
			() -> componentSupplier.getClassHunter().findBy(
				SearchConfig.forPaths(
						componentSupplier.getPathHelper().getMainClassPaths()
				).by(
					ClassCriteria.create().allThat((cls) -> {
						return cls.getAnnotations() != null && cls.getAnnotations().length > 0;
					}).or().byMembers(
						MethodCriteria.byScanUpTo((lastClassInHierarchy, currentScannedClass) -> {
							return lastClassInHierarchy.equals(currentScannedClass);
						}).allThat((method) -> {
							return method.getAnnotations() != null && method.getAnnotations().length > 0;
						})
					)
				).isolateClassLoader()
			),
			(result) ->
				result.getClasses()
		);
	}
}
