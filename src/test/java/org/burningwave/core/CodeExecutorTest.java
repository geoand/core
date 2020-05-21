package org.burningwave.core;

import java.util.ArrayList;
import java.util.List;

import org.burningwave.core.assembler.ComponentSupplier;
import org.burningwave.core.classes.ExecuteConfig;
import org.junit.jupiter.api.Test;

public class CodeExecutorTest extends BaseTest {
	
	@Test
	public void executeCodeTest() throws Exception {
		ComponentSupplier componentSupplier = getComponentSupplier();
		testNotNull(() -> {
			return componentSupplier.getCodeExecutor().execute(
				ExecuteConfig.fromStatementSourceGenerator()
				.useType(ArrayList.class, List.class)
				.addCodeRow("System.out.println(\"number to add: \" + parameter[0]);")
				.addCodeRow("List<Integer> numbers = new ArrayList<>();")
				.addCodeRow("numbers.add((Integer)parameter[0]);")
				.addCodeRow("System.out.println(\"number list size: \" + numbers.size());")
				.addCodeRow("System.out.println(\"number in the list: \" + numbers.get(0));")
				.addCodeRow("Integer inputNumber = (Integer)parameter[0];")
				.addCodeRow("return (T)inputNumber++;")		
				.withParameter(Integer.valueOf(5))
			);
		});
	}
	
	@Test
	public void executeCodeOfPropertiesFileTest() throws Exception {
		ComponentSupplier componentSupplier = getComponentSupplier();
		testNotNull(() -> {
			return componentSupplier.getCodeExecutor().execute(
				ExecuteConfig.fromPropertiesFile("code.properties").setPropertyName("code-block-1")
			);
		});
	}
}