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
package org.burningwave.core.classes.source;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public class Statement extends Generator.Abst {
	private String startingDelimiter;
	private String endingDelimiter;
	private String elementPrefix;
	private String elementSeparator;
	private Collection<Generator> bodyGenerators;
		
	private Statement() {}
	
	public static Statement create() {
		return new Statement().setDelimiters("{", "\n}").setElementPrefix("\t");
	}
	
	public static Statement createSimple() {
		return new Statement().setDelimiters(null, null).setElementPrefix(null); 
	}
	
	public Statement setBodyElementSeparator(String elementSeparator) {
		this.elementSeparator = elementSeparator;
		return this;
	}
	
	public Statement setElementPrefix(String elementPrefix) {
		this.elementPrefix = elementPrefix;
		return this;
	}
	
	public Statement setDelimiters(String startingDelimiter, String endingDelimiter) {
		this.startingDelimiter = startingDelimiter;
		this.endingDelimiter = endingDelimiter;
		return this;
	}
	
	public Statement addElement(Generator generator) {
		this.bodyGenerators = Optional.ofNullable(this.bodyGenerators).orElseGet(ArrayList::new);
		this.bodyGenerators.add(generator);
		return this;		
	}
	
	public Statement addCode(String element) {
		this.bodyGenerators = Optional.ofNullable(this.bodyGenerators).orElseGet(ArrayList::new);
		this.bodyGenerators.add(new Generator() {
			@Override
			public String make() {
				return element;
			}
		});
		return this;
	}

	public Statement addCodeRow(String code) {
		return addCode("\n" + Optional.ofNullable(elementPrefix).orElseGet(() -> "") + code);		
	}
	
	public Statement addAllElements(Collection<? extends Generator> generators) {
		this.bodyGenerators = Optional.ofNullable(this.bodyGenerators).orElseGet(ArrayList::new);
		this.bodyGenerators.addAll(generators);
		return this;		
	}
	
	@Override
	public String make() {
		return getOrEmpty(startingDelimiter, getOrEmpty((Collection<?>)bodyGenerators, (String)Optional.ofNullable(elementSeparator).orElse(EMPTY_SPACE)), endingDelimiter);
	}
	
}
