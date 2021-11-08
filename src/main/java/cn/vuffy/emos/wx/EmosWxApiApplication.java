package cn.vuffy.emos.wx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
// 主类添加@ServletComponentScan注解使filter生效
@ServletComponentScan
public class EmosWxApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmosWxApiApplication.class, args);
	}

}
