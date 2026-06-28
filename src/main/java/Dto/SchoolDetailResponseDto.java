package Dto;





import java.util.List;
import java.util.Map;

import com.menu.demo.Enums.SubscriptionStatus;

import lombok.Builder;
import lombok.Data;

//The full school detail page response
@Data @Builder
public class SchoolDetailResponseDto {

 // ── School info ──────────────────────────────
 private Long   schoolId;
 private String schoolName;
 private String ownerName;
 private String phone;
 private String email;
 private String address;
 private String wilaya;
 private String commune;
 private SubscriptionStatus subscriptionStatus;

 // ── Stats ────────────────────────────────────
 private long totalTeachers;
 private long totalModules;
 private long totalStudents;

 // ── Teachers with their subjects ─────────────
 private List<TeacherWithSubjectsDto> teachers;

 // ── Modules grouped by day ───────────────────
 private Map<String, List<ModuleScheduleViewDto>> modulesByDay;
}
