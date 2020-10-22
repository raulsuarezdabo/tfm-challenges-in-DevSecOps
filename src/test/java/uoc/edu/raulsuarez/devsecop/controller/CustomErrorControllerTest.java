package uoc.edu.raulsuarez.devsecop.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class CustomErrorControllerTest {

    @Autowired
    CustomErrorController controller;

    @Test
    public void getErrorPathTest() {
        assertThat(controller.getErrorPath()).isNull();
    }

    @Test
    public void handleErrorTest() {
        assertThat(controller.handleError()).isEqualTo("error");
    }

}
