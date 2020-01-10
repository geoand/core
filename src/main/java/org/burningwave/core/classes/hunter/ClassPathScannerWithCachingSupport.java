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
package org.burningwave.core.classes.hunter;


import java.io.File;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.burningwave.core.classes.ClassCriteria;
import org.burningwave.core.classes.ClassHelper;
import org.burningwave.core.classes.MemberFinder;
import org.burningwave.core.classes.hunter.SearchContext.InitContext;
import org.burningwave.core.common.Strings;
import org.burningwave.core.io.FileSystemHelper;
import org.burningwave.core.io.PathHelper;
import org.burningwave.core.io.PathHelper.CheckResult;
import org.burningwave.core.io.StreamHelper;


public abstract class ClassPathScannerWithCachingSupport<K, I, C extends SearchContext<K, I>, R extends SearchResult<K, I>> extends ClassPathScanner<K, I, C, R> implements org.burningwave.core.Component {
	Map<String, Map<K, I>> cache;

	ClassPathScannerWithCachingSupport(
		Supplier<ByteCodeHunter> byteCodeHunterSupplier,
		Supplier<ClassHunter> classHunterSupplier,
		FileSystemHelper fileSystemHelper,
		PathHelper pathHelper,
		StreamHelper streamHelper,
		ClassHelper classHelper,
		MemberFinder memberFinder,
		Function<InitContext, C> contextSupplier,
		Function<C, R> resultSupplier) {
		super(
			byteCodeHunterSupplier,
			classHunterSupplier,
			fileSystemHelper,
			pathHelper,
			streamHelper,
			classHelper,
			memberFinder,
			contextSupplier,
			resultSupplier
		);
		this.cache = new ConcurrentHashMap<>();
	}
	
	//Cached search
	public R findBy(CacheableSearchConfig searchConfig) {
		searchConfig = searchConfig.createCopy();
		C context = createContext(
			ClassFileScanConfig.forPaths(searchConfig.getPaths()).maxParallelTasksForUnit(
				searchConfig.maxParallelTasksForUnit
			), 
			searchConfig
		);
		searchConfig.init(this.classHelper, context.pathMemoryClassLoader, this.memberFinder);
		context.executeSearch(() ->
			scan(context)
		);
		Collection<String> skippedClassesNames = context.getSkippedClassNames();
		if (!skippedClassesNames.isEmpty()) {
			logWarn("Skipped classes count: {}", skippedClassesNames.size());
		}
		return resultSupplier.apply(context);
	}
	
	
	void scan(C context) {
		Collection<String> pathsNotScanned = scanCache(context);
		if (!pathsNotScanned.isEmpty()) {
			if (context.getSearchConfig().getClassCriteria().hasNoPredicate()) {
				synchronized (cache) {
					pathsNotScanned = scanCache(context);
					if (!pathsNotScanned.isEmpty()) {
						for (String path : pathsNotScanned) {
							Map<K, I> classesForPath = cache.get(path);
							if (classesForPath != null && !classesForPath.isEmpty()) {
								context.addAllItemsFound(path,classesForPath);
								pathsNotScanned.remove(path);
							}
						}
						if (!pathsNotScanned.isEmpty()) {
							loadCache(context, pathsNotScanned);
						}
					}
				}
			} else {
				fileSystemHelper.scan(
					context.classFileScanConfiguration.createCopy().setPaths(pathsNotScanned).toScanConfiguration(context, this)				
				);
			}
		}
	}
	
	Collection<String> scanCache(C context) {
		Collection<String> pathsNotScanned = new LinkedHashSet<>();
		CacheableSearchConfig searchConfig = context.getSearchConfig();
		if (!context.getSearchConfig().getClassCriteria().hasNoPredicate()) {
			for (String path : searchConfig.getPaths()) {
				Map<K, I> classesForPath = cache.get(path);
				if (classesForPath != null) {
					if (!classesForPath.isEmpty()) {	
						iterateAndTestItemsForPath(context, path, classesForPath);
					}
				} else {
					pathsNotScanned.add(path);
				}
			}
		} else {
			for (String path : searchConfig.getPaths()) {
				Map<K, I> classesForPath = cache.get(path);
				if (classesForPath != null) {
					if (!classesForPath.isEmpty()) {
						context.addAllItemsFound(path, classesForPath);
					}
				} else {
					pathsNotScanned.add(path);
				}
			}
		}
		return pathsNotScanned;
	}
	
	public void loadCache(Collection<String> paths) {
		try (SearchResult<K, I> result = 
			findBy(
				SearchConfig.forPaths(paths)
			)
		) {}
	}
	

	void loadCache(C context, Collection<String> paths) {
		CheckResult checkPathsResult = pathHelper.check(cache.keySet(), paths);
		ClassFileScanConfig classFileScanConfiguration = context.classFileScanConfiguration.createCopy().setPaths(checkPathsResult.getNotContainedPaths());
		Map<String, Map<K, I>> tempCache = new LinkedHashMap<>();
		if (!checkPathsResult.getPartialContainedDirectories().isEmpty()) {
			Predicate<File> directoryPredicate = null;
			for (Entry<String, Collection<String>> entry : checkPathsResult.getPartialContainedDirectories().entrySet()) {
				for (String path : entry.getValue()) {
					tempCache.put(entry.getKey(), cache.get(path));
					if (directoryPredicate != null) {
						directoryPredicate.and(file -> !(Strings.Paths.clean(file.getAbsolutePath()) + "/").startsWith(Strings.Paths.clean(path) + "/"));
					} else {
						directoryPredicate = file -> !(Strings.Paths.clean(file.getAbsolutePath()) + "/").startsWith(Strings.Paths.clean(path) + "/");
					}
				}
			}
			if (directoryPredicate != null) {
				classFileScanConfiguration.scanRecursivelyAllDirectoryThat(directoryPredicate);
			}
		}
		if (!checkPathsResult.getPartialContainedFiles().isEmpty()) {
			Predicate<File> filePredicate = null;
			for (Entry<String, Collection<String>> entry : checkPathsResult.getPartialContainedFiles().entrySet()) {
				for (String path : entry.getValue()) {
					tempCache.put(Strings.Paths.clean(entry.getKey()), cache.get(path));
					if (filePredicate != null) {
						filePredicate.and(file -> !(Strings.Paths.clean(file.getAbsolutePath())).equals(Strings.Paths.clean(path)));
					} else {
						filePredicate = file -> !(Strings.Paths.clean(file.getAbsolutePath())).equals(Strings.Paths.clean(path));
					}
				}
			}
			if (filePredicate != null) {
				classFileScanConfiguration.scanAllLibraryFileThat(filePredicate);
				classFileScanConfiguration.scanAllClassFileThat(filePredicate);
			}
		}
	
		fileSystemHelper.scan(
			classFileScanConfiguration.toScanConfiguration(
				context, this
			).afterScanPath((mainScanContext, path) -> {
				mainScanContext.waitForTasksEnding();
				Map<K, I> itemsForPath = new ConcurrentHashMap<>();
				itemsForPath.putAll(context.getItemsFound(path));
				this.cache.put(path, itemsForPath);
			})
		);
		if (!tempCache.isEmpty()) {
			for (Entry<String, Map<K, I>> entry : tempCache.entrySet()) {
				cache.get(entry.getKey()).putAll(entry.getValue());
				context.addAllItemsFound(entry.getKey(), entry.getValue());
			}
		}
	}
	

	<S extends SearchConfigAbst<S>> void iterateAndTestItemsForPath(C context, String path, Map<K, I> itemsForPath) {
		for (Entry<K, I> cachedItemAsEntry : itemsForPath.entrySet()) {
			ClassCriteria.TestContext testContext = testCachedItem(context, path, cachedItemAsEntry.getKey(), cachedItemAsEntry.getValue());
			if(testContext.getResult()) {
				addCachedItemToContext(context, testContext, path, cachedItemAsEntry);
			}
		}
	}
	
	
	<S extends SearchConfigAbst<S>> void addCachedItemToContext(
		C context, ClassCriteria.TestContext testContext, String path, Entry<K, I> cachedItemAsEntry
	) {
		context.addItemFound(path, cachedItemAsEntry.getKey(), cachedItemAsEntry.getValue());
	}

	abstract <S extends SearchConfigAbst<S>> ClassCriteria.TestContext testCachedItem(C context, String path, K key, I value);
	
	
	@Override
	public void close() {
		cache.clear();
		cache.entrySet().stream().forEach(entry -> {
			entry.getValue().clear();
		});
		cache = null;
		byteCodeHunterSupplier = null;
		classHelper = null;
		fileSystemHelper = null;
		streamHelper = null;
		pathHelper = null;
		contextSupplier = null;
	}
}