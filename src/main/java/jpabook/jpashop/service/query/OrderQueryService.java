package jpabook.jpashop.service.query;

import org.springframework.transaction.annotation.Transactional;

/**
 * 쿼리형 서비스는 별도로 분리하는 것이 좋다.
 * 아래에 변환 로직을 넣어두면, 영속성 컨텍스트와 DB session 을 들고 있다.
 * 따라서 로직을 수행할 수 있다.
 */
@Transactional(readOnly = true)
public class OrderQueryService {

}
