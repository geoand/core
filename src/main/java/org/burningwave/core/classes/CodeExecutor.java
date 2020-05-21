/*
 * This file is part of Burningwave Core.
 *
 * Author: Roberto Gentili
 *
 * Hosted at: https://github.com/burningwave/core
 *
 * --
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2019 Roberto Gentili
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without
 * limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO
 * EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.burningwave.core.classes;

import static org.burningwave.core.assembler.StaticComponentContainer.Constructors;
import static org.burningwave.core.assembler.StaticComponentContainer.Strings;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import org.burningwave.core.Component;
import org.burningwave.core.Executor;
import org.burningwave.core.assembler.ComponentSupplier;
import org.burningwave.core.function.ThrowingRunnable;
import org.burningwave.core.function.ThrowingSupplier;
import org.burningwave.core.io.FileSystemItem;
import org.burningwave.core.io.PathHelper;
import org.burningwave.core.iterable.IterableObjectHelper;
import org.burningwave.core.iterable.Properties;

public class CodeExecutor implements Component {
	public final static String PROPERTIES_FILE_CODE_EXECUTOR_IMPORTS_KEY_SUFFIX = ".imports";
	
	private SourceCodeHandler sourceCodeHandler;
	private ClassFactory classFactory;
	private PathHelper pathHelper;
	private Supplier<ClassFactory> classFactorySupplier;
	private IterableObjectHelper iterableObjectHelper;	
	private Supplier<IterableObjectHelper> iterableObjectHelperSupplier;
	private Properties config;
	
	private CodeExecutor(
		Supplier<ClassFactory> classFactorySupplier,
		SourceCodeHandler sourceCodeHandler,
		PathHelper pathHelper,
		Supplier<IterableObjectHelper> iterableObjectHelperSupplier,
		Properties config
	) {	
		this.classFactorySupplier = classFactorySupplier;
		this.sourceCodeHandler = sourceCodeHandler;
		this.pathHelper = pathHelper;
		this.iterableObjectHelperSupplier = iterableObjectHelperSupplier;
		this.config = config;
		listenTo(config);
	}
		
	public static CodeExecutor create(
		Supplier<ClassFactory> classFactorySupplier,
		SourceCodeHandler sourceCodeHandler,
		PathHelper pathHelper,
		Supplier<IterableObjectHelper> iterableObjectHelperSupplier,
		Properties config
	) {
		return new CodeExecutor(
			classFactorySupplier,
			sourceCodeHandler, 
			pathHelper,
			iterableObjectHelperSupplier,
			config
		);
	}
	
	private ClassFactory getClassFactory() {
		return classFactory != null? classFactory :
			(classFactory = classFactorySupplier.get());
	}
	
	protected IterableObjectHelper getIterableObjectHelper() {
		return iterableObjectHelper != null ?
			iterableObjectHelper :
			(iterableObjectHelper = iterableObjectHelperSupplier.get());
	}
	
	public <T extends Executor> Class<T> loadOrBuildAndDefineExecutorSubType(String className, StatementSourceGenerator statement) {
		return loadOrBuildAndDefineExecutorSubType(getClassFactory().getDefaultClassLoader(), className, statement);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Executor> Class<T> loadOrBuildAndDefineExecutorSubType(ClassLoader classLoader, String className, StatementSourceGenerator statement) {
		return (Class<T>) getClassFactory().loadOrBuildAndDefine(
			LoadOrBuildAndDefineConfig.forUnitSourceGenerator(
				sourceCodeHandler.generateExecutor(className, statement)
			).useClassLoader(
				classLoader
			)
		).get(
			className
		);
	}
	
	public <T> T execute(String propertyName) {
		return execute(ExecuteConfig.ForProperties.fromProperty(propertyName));
	}
	
	public <T> T execute(ExecuteConfig.ForProperties config) {
		ClassLoader parentClassLoader = config.getParentClassLoader();
		if (parentClassLoader == null && config.isUseDefaultClassLoaderAsParentIfParentClassLoaderIsNull()) {
			parentClassLoader = getClassFactory().getDefaultClassLoader();
		}
		
		java.util.Properties properties = config.getProperties();
		if (properties == null) {
			if (config.getFilePath() == null) {
				properties = this.config; 
			} else {
				Properties tempProperties = new Properties();
				if (config.isAbsoluteFilePath()) {
					ThrowingRunnable.run(() -> 
						tempProperties.load(FileSystemItem.ofPath(config.getFilePath()).toInputStream())
					);
				} else {
					ThrowingRunnable.run(() ->
						tempProperties.load(pathHelper.getResourceAsStream(config.getFilePath()))
					);
				}
				properties = tempProperties;
			}
			
		}
		return execute(parentClassLoader, properties, config.getPropertyName(), config.getDefaultValues(), config.getParams());
	}		
	
	private <T> T execute(
		ClassLoader classLoaderParent,
		java.util.Properties properties, 
		String key,
		Map<String, String> defaultValues,
		Object... params
	) {	
		StatementSourceGenerator statement = StatementSourceGenerator.createSimple().setElementPrefix("\t");
		if (params != null && params.length > 0) {
			for (Object param : params) {
				statement.useType(param.getClass());
			}
		}
		String importFromConfig = getIterableObjectHelper().get(properties, key + PROPERTIES_FILE_CODE_EXECUTOR_IMPORTS_KEY_SUFFIX, defaultValues);
		if (Strings.isNotEmpty(importFromConfig)) {
			Arrays.stream(importFromConfig.split(";")).forEach(imp -> {
				statement.useType(imp);
			});
		}
		String code = getIterableObjectHelper().get(properties, key, defaultValues);
		if (code.contains(";")) {
			for (String codeRow : code.split(";")) {
				statement.addCodeRow(codeRow + ";");
			}
		} else {
			statement.addCodeRow(code.contains("return")?
				code:
				"return (T)" + code + ";"
			);
		}
		return execute(
			classLoaderParent, statement, params
		);
	}
	
	public <T> T execute(StatementSourceGenerator statement) {
		return execute(ExecuteConfig.from(statement));
	}
	
	public <T> T execute(
		ExecuteConfig.ForStatementSourceGenerator config
	) {	
		ClassLoader parentClassLoader = config.getParentClassLoader();
		if (parentClassLoader == null && config.isUseDefaultClassLoaderAsParentIfParentClassLoaderIsNull()) {
			parentClassLoader = getClassFactory().getDefaultClassLoader();
		}
		return execute(parentClassLoader, config.getStatement(), config.getParams());
	}
	
	private <T> T execute(
		ClassLoader classLoaderParentOfOneShotClassLoader,
		StatementSourceGenerator statement,
		Object... parameters
	) {	
		return ThrowingSupplier.get(() -> {
			try (MemoryClassLoader memoryClassLoader = 
				MemoryClassLoader.create(
					classLoaderParentOfOneShotClassLoader
				)
			) {
				String packageName = Executor.class.getPackage().getName();
				Class<? extends Executor> executableClass = loadOrBuildAndDefineExecutorSubType(
					memoryClassLoader, packageName + ".CodeExecutor_" + UUID.randomUUID().toString().replaceAll("-", ""), statement
				);
				Executor executor = Constructors.newInstanceOf(executableClass);
				ComponentSupplier componentSupplier = null;
				if (parameters != null && parameters.length > 0) {
					for (Object param : parameters) {
						if (param instanceof ComponentSupplier) {
							componentSupplier = (ComponentSupplier) param;
							break;
						}
					}
				}
				T retrievedElement = executor.execute(componentSupplier, parameters);
				return retrievedElement;
			}
		});
	}
}