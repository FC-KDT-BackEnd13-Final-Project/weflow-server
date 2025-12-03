package com.rdc.weflow_server.repository.attachment;

import com.rdc.weflow_server.entity.attachment.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    List<Attachment> findByTargetTypeAndTargetId(Attachment.TargetType targetType, Long targetId);

    // 특정 타입의 첨부파일 개수 조회
    int countByTargetTypeAndTargetIdAndAttachmentType(
            Attachment.TargetType targetType,
            Long targetId,
            Attachment.AttachmentType attachmentType
    );

    boolean existsByIdAndTargetTypeAndTargetId(Long id, Attachment.TargetType targetType, Long targetId);

    // 특정 타입의 첨부파일 삭제
    void deleteByTargetTypeAndTargetIdAndAttachmentType(
            Attachment.TargetType targetType,
            Long targetId,
            Attachment.AttachmentType attachmentType
    );

    void deleteByTargetTypeAndTargetId(Attachment.TargetType targetType, Long targetId);
}
