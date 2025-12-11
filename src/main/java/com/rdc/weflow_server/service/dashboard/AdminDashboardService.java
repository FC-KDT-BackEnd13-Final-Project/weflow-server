package com.rdc.weflow_server.service.dashboard;

import com.rdc.weflow_server.dto.dashboard.AdminDashboardResponse;
import com.rdc.weflow_server.dto.log.ActivityLogResponseDto;
import com.rdc.weflow_server.repository.company.CompanyRepository;
import com.rdc.weflow_server.repository.log.ActivityLogRepository;
import com.rdc.weflow_server.repository.project.ProjectRepository;
import com.rdc.weflow_server.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final ProjectRepository projectRepository;
    private final ActivityLogRepository activityLogRepository;

    public AdminDashboardResponse getDashboard() {

        long totalUsers = userRepository.countActiveUsers();
        long totalCompanies = companyRepository.countActiveCompanies();
        long totalProjects = projectRepository.countActiveProjects();

        // 최근 로그 5개만 가져오기
        List<ActivityLogResponseDto> recentLogs =
                activityLogRepository.findRecentLogs(5);

        return AdminDashboardResponse.builder()
                .totalUsers(totalUsers)
                .totalCompanies(totalCompanies)
                .totalProjects(totalProjects)
                .recentLogs(recentLogs)
                .build();
    }
}
