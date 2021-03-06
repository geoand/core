package org.burningwave.core.classes;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

class LoadOrBuildAndDefineConfigAbst<L extends LoadOrBuildAndDefineConfigAbst<L>> {
	
	private Collection<String> compilationClassPaths;
	private Collection<String> classPathsWhereToSearchNotFoundClassesDuringCompilation;
	private Collection<String> classPathsWhereToSearchNotFoundClassesDuringLoading;
	private Collection<UnitSourceGenerator> unitSourceGenerators;
	private ClassLoader classLoader;
	private boolean useOneShotJavaCompiler;
	private boolean storeCompiledClasses;
		
	@SafeVarargs LoadOrBuildAndDefineConfigAbst(UnitSourceGenerator... unitsCode) {
		this(Arrays.asList(unitsCode));
		this.storeCompiledClasses = true;
	}
	
	@SafeVarargs
	LoadOrBuildAndDefineConfigAbst(Collection<UnitSourceGenerator>... unitCodeCollections) {
		unitSourceGenerators = new HashSet<>();
		for (Collection<UnitSourceGenerator> unitsCode : unitCodeCollections) {
			unitSourceGenerators.addAll(unitsCode);
		}
	}
	
	@SuppressWarnings("unchecked")
	@SafeVarargs
	public final L addCompilationClassPaths(Collection<String>... classPathCollections) {
		if (compilationClassPaths == null) {
			compilationClassPaths = new HashSet<>();
		}
		for (Collection<String> classPathCollection : classPathCollections) {
			compilationClassPaths.addAll(classPathCollection);
		}
		return (L)this;
	}
	
	@SafeVarargs
	public final L addCompilationClassPaths(String... classPaths) {
		return addCompilationClassPaths(Arrays.asList(classPaths));
	}
	
	@SuppressWarnings("unchecked")
	@SafeVarargs
	public final L addClassPathsWhereToSearchNotFoundClassesDuringCompilation(Collection<String>... classPathCollections) {
		if (classPathsWhereToSearchNotFoundClassesDuringCompilation == null) {
			classPathsWhereToSearchNotFoundClassesDuringCompilation = new HashSet<>();
		}
		for (Collection<String> classPathCollection : classPathCollections) {
			classPathsWhereToSearchNotFoundClassesDuringCompilation.addAll(classPathCollection);
		}
		return (L)this;
	}
	
	@SafeVarargs
	public final L addClassPathsWhereToSearchNotFoundClassesDuringCompilation(String... classPaths) {
		return addClassPathsWhereToSearchNotFoundClassesDuringCompilation(Arrays.asList(classPaths));
	}
	
	@SafeVarargs
	public final L addClassPathsWhereToSearchNotFoundClasses(String... classPaths) {
		return addClassPathsWhereToSearchNotFoundClassesDuringCompilation(classPaths)
			.addClassPathsWhereToSearchNotFoundClassesDuringLoading(classPaths);		
	}
	
	@SafeVarargs
	public final L addClassPathsWhereToSearchNotFoundClasses(Collection<String>... classPathCollections) {
		return addClassPathsWhereToSearchNotFoundClassesDuringCompilation(classPathCollections)
			.addClassPathsWhereToSearchNotFoundClassesDuringLoading(classPathCollections);		
	}
	
	@SuppressWarnings("unchecked")
	@SafeVarargs
	public final L addClassPathsWhereToSearchNotFoundClassesDuringLoading(Collection<String>... classPathCollections) {
		if (classPathsWhereToSearchNotFoundClassesDuringLoading == null) {
			classPathsWhereToSearchNotFoundClassesDuringLoading = new HashSet<>();
		}
		for (Collection<String> classPathCollection : classPathCollections) {
			classPathsWhereToSearchNotFoundClassesDuringLoading.addAll(classPathCollection);
		}
		return (L)this;
	}
	
	@SafeVarargs
	public final L addClassPathsWhereToSearchNotFoundClassesDuringLoading(String... classPaths) {
		return (L)addClassPathsWhereToSearchNotFoundClassesDuringLoading(Arrays.asList(classPaths));
	}
	
	@SuppressWarnings("unchecked")
	public L useClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
		return (L)this;
	}
	
	@SuppressWarnings("unchecked")
	public L useOneShotJavaCompiler(boolean flag) {
		this.useOneShotJavaCompiler = flag;
		return (L)this;
	}
	
	@SuppressWarnings("unchecked")
	public L storeCompiledClasses(boolean flag) {
		this.storeCompiledClasses = flag;
		return (L)this;
	}
	
	Collection<String> getCompilationClassPaths() {
		return compilationClassPaths;
	}

	Collection<String> getClassPathsWhereToSearchNotFoundClassesDuringCompilation() {
		return classPathsWhereToSearchNotFoundClassesDuringCompilation;
	}

	Collection<String> getClassPathsWhereToSearchNotFoundClassesDuringLoading() {
		return classPathsWhereToSearchNotFoundClassesDuringLoading;
	}
	
	Collection<UnitSourceGenerator> getUnitSourceGenerators() {
		return unitSourceGenerators;
	}

	ClassLoader getClassLoader() {
		return classLoader;
	}

	boolean isUseOneShotJavaCompiler() {
		return useOneShotJavaCompiler;
	}
	
	boolean isStoreCompiledClasses() {
		return storeCompiledClasses;
	}
}