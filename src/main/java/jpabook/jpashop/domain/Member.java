package jpabook.jpashop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Member {
    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    @NotEmpty
    private String name;

    @Embedded
    private Address address;

//    @JsonIgnore // 회원 정보만 필요하니까, order 정보는 무시하겠다는 어노테이션
//    // 그러나, 다양한 API 스펙이 있을 수 있고, 화면에 뿌리기 위한 로직이 들어가버리므로, 사용 x
    @OneToMany(mappedBy = "member")
    private List<Order> orders = new ArrayList<>();

}
