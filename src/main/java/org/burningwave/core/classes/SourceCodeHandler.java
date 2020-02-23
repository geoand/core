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

import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.burningwave.core.Component;
import org.burningwave.core.Strings;
import org.burningwave.core.Virtual;
import org.burningwave.core.assembler.ComponentSupplier;
import org.burningwave.core.function.ThrowingSupplier;


public class SourceCodeHandler implements Component {
	private ClassFactory classFactory;
	private Supplier<ClassFactory> classFactorySupplier;
	private Classes classes;
	private Classes.Loaders classesLoaders;

	private SourceCodeHandler(
		Supplier<ClassFactory> classFactorySupplier,
		Classes classes,
		Classes.Loaders classesLoaders
	) {
		this.classFactorySupplier = classFactorySupplier;
		this.classes = classes;
		this.classesLoaders = classesLoaders;
	}
	
	public static SourceCodeHandler create(
		Supplier<ClassFactory> classFactorySupplier,
		Classes classes,
		Classes.Loaders classesLoaders
	) {
		return new SourceCodeHandler(classFactorySupplier, classes, classesLoaders);
	}
	
	private ClassFactory getClassFactory() {
		return classFactory != null? classFactory : (classFactory = classFactorySupplier.get());
	}
	
	public String extractClassName(String classCode) {
		return
			Optional.ofNullable(
				Strings.extractAllGroups(
					Pattern.compile("(package)\\s*([[a-zA-Z0-9\\s]*\\.?]*)"), classCode
				).get(2).get(0)
			).map(
				value -> value + "."
			).orElse("") +
			Strings.extractAllGroups(
				Pattern.compile("(?<=\\n|\\A)(?:public\\s*)?(class|interface|enum)\\s*([^\\n\\s<]*)"), classCode
			).get(2).get(0);
	}
	
	
	public <T> T execute(
		String imports, 
		String className, 
		String supplierCode, 
		ComponentSupplier componentSupplier,
		ClassLoader classLoaderParentOfOneShotClassLoader,
		Object... parameters
	) {	
		return ThrowingSupplier.get(() -> {
			try (MemoryClassLoader memoryClassLoader = 
				MemoryClassLoader.create(
					classLoaderParentOfOneShotClassLoader,
					classes,
					classesLoaders
				)
			) {
				Class<?> virtualClass = getClassFactory().getOrBuildCodeExecutorSubType(
					imports, className, supplierCode, componentSupplier, memoryClassLoader
				);
				Virtual virtualObject = ((Virtual)virtualClass.getDeclaredConstructor().newInstance());
				T retrievedElement = virtualObject.invokeWithoutCachingMethod("execute", componentSupplier, parameters);
				return retrievedElement;
			}
		});
	}
	
	@Override
	public void close() {
		classes = null;
		classFactory = null;
		classFactorySupplier = null;
	}
}