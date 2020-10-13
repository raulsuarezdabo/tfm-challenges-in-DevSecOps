package uoc.edu.raulsuarez.devsecop.service;


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class CalculatorServiceTest {

    @Autowired
    private CalculatorService calculatorService;

    private static Integer[] integers;

    @BeforeAll
    public static void init() {
        integers = new Integer[] {1,2,3};
    }

    @Test
    public void contextLoads() throws Exception {
        assertThat(this.calculatorService).isNotNull();
    }

    @Test
    public void additionTestOK() {
        Integer result = calculatorService.addition(0,integers);
        assertThat(result).isEqualTo(Integer.valueOf(6));
    }

    @Test
    public void substractionTestOK() {
        Integer result = calculatorService.subtraction(0,integers);
        assertThat(result).isEqualTo(Integer.valueOf(-6));
    }

    @Test
    public void multiplicationTestOK() {
        Integer result = calculatorService.multiplication(1,integers);
        assertThat(result).isEqualTo(Integer.valueOf(6));
    }

    @Test
    public void divisionTestOK() {
        Integer result = calculatorService.division(1,integers);
        assertThat(result).isEqualTo(Integer.valueOf(0));
    }
}
