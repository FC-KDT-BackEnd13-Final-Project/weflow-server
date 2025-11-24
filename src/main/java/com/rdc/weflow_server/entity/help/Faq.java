package com.rdc.weflow_server.entity.help;

import com.rdc.weflow_server.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@Entity
@Table(name = "faq")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Faq extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 질문 */
    @Column(nullable = false, length = 255)
    private String question;

    /** 답변 */
    @Column(columnDefinition = "TEXT")
    private String answer;

    /** 정렬 순서 */
    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;
}
