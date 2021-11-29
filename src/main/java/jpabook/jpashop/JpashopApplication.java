package jpabook.jpashop;

import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class JpashopApplication {

	public static void main(String[] args) {
		SpringApplication.run(JpashopApplication.class, args);
	}

	/**
	 * 아래 메서드와 같이는 쓰지 말 것.
	 * @return
	 */
	@Bean Hibernate5Module hibernate5Module() {
		Hibernate5Module hibernate5Module = new Hibernate5Module();
		// 강제로 지연로딩 해서 엔티티 정보를 가져오도록 설정 한다.
//		 hibernate5Module.configure(Hibernate5Module.Feature.FORCE_LAZY_LOADING, true);
		 return hibernate5Module;
	}
}
