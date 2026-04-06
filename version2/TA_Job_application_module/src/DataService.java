import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/**
 * 数据服务类 - 从data/jobs文件夹读取JSON文件
 */
public class DataService {
    private static DataService instance;
    private TAUser currentUser;
    private List<Job> jobs;
    private List<Application> applications;
    private int nextApplicationId = 2;
    private final Gson gson;
    
    // data文件夹的路径 - 指向system目录下的data文件夹
    // 这样可以与项目外的其他模块共享同一份数据
    // 使用用户工作目录或项目根目录
    private static final String DATA_ROOT;
    
    static {
        // 优先使用系统属性指定的data路径，否则使用相对于当前工作目录的路径
        String dataPath = System.getProperty("taportal.data.path");
        if (dataPath != null && !dataPath.isEmpty()) {
            DATA_ROOT = dataPath;
        } else {
            // 相对于当前工作目录的data文件夹
            DATA_ROOT = "data";
        }
        
        // 调试：打印实际使用的数据根目录
        System.out.println("[DataService] DATA_ROOT = " + DATA_ROOT);
    }
    
    private static final String JOBS_FOLDER = DATA_ROOT + File.separator + "jobs";
    private static final String APPLICATIONS_FOLDER = DATA_ROOT + File.separator + "applications";
    
    /**
     * 获取Jobs文件夹的绝对路径，尝试多个可能的位置
     */
    private File findJobsDirectory() {
        // 尝试的路径列表（按优先级从高到低）
        String userDir = System.getProperty("user.dir");
        String[][] possiblePaths = {
            // 0. 父级目录的 data/jobs（src/data，与 version2 同级）
            { userDir + File.separator + ".." + File.separator + ".." + File.separator + ".." + File.separator + "data" + File.separator + "jobs", "父级src目录（3级）" },
            // 1. system根目录下的data/jobs（用户最可能的数据位置）
            { userDir + File.separator + "data" + File.separator + "jobs", "system根目录" },
            // 2. ta-portal/data/jobs（相对于ta-portal模块）
            { userDir + File.separator + "ta-portal" + File.separator + "data" + File.separator + "jobs", "ta-portal目录" },
            // 3. 相对路径 ../../../data/jobs（从src向上3级）
            { ".." + File.separator + ".." + File.separator + ".." + File.separator + "data" + File.separator + "jobs", "相对路径（3级）" }
        };
        
        for (String[] pathInfo : possiblePaths) {
            String path = pathInfo[0];
            String source = pathInfo[1];
            File dir = new File(path);
            if (dir.exists() && dir.isDirectory()) {
                // 检查目录里是否有JSON文件
                File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
                int fileCount = files != null ? files.length : 0;
                System.out.println("Found jobs directory at [" + source + "]: " + dir.getAbsolutePath() + " (" + fileCount + " JSON files)");
                return dir;
            } else {
                System.out.println("Not found [" + source + "]: " + path);
            }
        }
        
        // 返回默认路径
        System.out.println("Using default path: " + JOBS_FOLDER);
        return new File(JOBS_FOLDER);
    }

    /**
     * 获取Applications文件夹的绝对路径，尝试多个可能的位置
     */
    private File findApplicationsDirectory() {
        String userDir = System.getProperty("user.dir");
        String[][] possiblePaths = {
            { userDir + File.separator + ".." + File.separator + ".." + File.separator + ".." + File.separator + "data" + File.separator + "applications", "父级src目录（3级）" },
            { userDir + File.separator + "data" + File.separator + "applications", "system根目录" },
            { userDir + File.separator + "ta-portal" + File.separator + "data" + File.separator + "applications", "ta-portal目录" },
            { ".." + File.separator + ".." + File.separator + ".." + File.separator + "data" + File.separator + "applications", "相对路径（3级）" }
        };

        for (String[] pathInfo : possiblePaths) {
            String path = pathInfo[0];
            String source = pathInfo[1];
            File dir = new File(path);
            if (dir.exists() && dir.isDirectory()) {
                File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
                int fileCount = files != null ? files.length : 0;
                System.out.println("Found applications directory at [" + source + "]: " + dir.getAbsolutePath() + " (" + fileCount + " JSON files)");
                return dir;
            }
        }
        System.out.println("Using default applications path: " + APPLICATIONS_FOLDER);
        return new File(APPLICATIONS_FOLDER);
    }
    
    /**
     * 保存单个申请到JSON文件
     */
    private void saveApplicationToFile(Application app) {
        try {
            File dir = findApplicationsDirectory();
            // 确保目录存在
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(dir, app.getApplicationId() + ".json");
            
            // 使用Gson Pretty Printing美化输出
            Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
            String json = prettyGson.toJson(app);
            
            java.nio.file.Files.writeString(file.toPath(), json);
            System.out.println("Application saved to: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error saving application: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private DataService() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        loadMockData();
    }

    public static DataService getInstance() {
        if (instance == null) {
            instance = new DataService();
        }
        return instance;
    }

    private void loadMockData() {
        loadCurrentUser();
        loadJobs();
        loadApplications();
    }

    private void loadCurrentUser() {
        currentUser = new TAUser();
        currentUser.setUserId("u_ta_20230001");
        currentUser.setLoginId("20230001");
        currentUser.setRole("ta");
        currentUser.setProfileCompletion(85);

        TAUser.Account account = new TAUser.Account();
        account.setUsername("johnsmith");
        account.setEmail("john.smith@university.edu");
        account.setStatus("active");
        account.setLastLoginAt("2026-03-18T09:20:00");
        currentUser.setAccount(account);

        TAUser.Profile profile = new TAUser.Profile();
        profile.setFullName("John Smith");
        profile.setStudentId("20230001");
        profile.setYear("3rd Year");
        profile.setProgramMajor("Computer Science");
        profile.setDepartment("Computer Science");
        profile.setPhoneNumber("(555) 123-4567");
        profile.setAddress("123 Main St, City, State, ZIP");
        profile.setShortBio("Interested in programming education and software engineering.");
        currentUser.setProfile(profile);

        TAUser.Academic academic = new TAUser.Academic();
        academic.setGpa(3.8);
        List<TAUser.CompletedCourse> courses = new ArrayList<>();
        TAUser.CompletedCourse c1 = new TAUser.CompletedCourse();
        c1.setCourseCode("CS101");
        c1.setCourseName("Introduction to Programming");
        c1.setGrade("A");
        courses.add(c1);
        TAUser.CompletedCourse c2 = new TAUser.CompletedCourse();
        c2.setCourseCode("CS201");
        c2.setCourseName("Data Structures");
        c2.setGrade("A-");
        courses.add(c2);
        academic.setCompletedCourses(courses);
        currentUser.setAcademic(academic);

        TAUser.CV cv = new TAUser.CV();
        cv.setUploaded(true);
        cv.setOriginalFileName("John_Smith_CV.pdf");
        cv.setFilePath("data/uploads/profile_cv/20230001/current_cv.pdf");
        cv.setUploadedAt("2026-03-10T10:30:00");
        currentUser.setCv(cv);

        TAUser.ApplicationSummary summary = new TAUser.ApplicationSummary();
        summary.setTotalApplications(3);
        summary.setPending(1);
        summary.setUnderReview(1);
        summary.setAccepted(1);
        summary.setRejected(0);
        currentUser.setApplicationSummary(summary);
    }

    /**
     * 从data/jobs文件夹加载所有职位JSON文件
     */
    private void loadJobs() {
        jobs = new ArrayList<>();
        
        // 使用智能路径检测找到jobs目录
        File jobsDir = findJobsDirectory();
        
        if (!jobsDir.exists() || !jobsDir.isDirectory()) {
            System.out.println("Warning: Jobs directory not found at " + jobsDir.getAbsolutePath());
            // 如果目录不存在，使用硬编码数据作为后备
            loadJobsFromHardcoded();
            return;
        }
        
        // 获取所有.json文件
        File[] jobFiles = jobsDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (jobFiles == null || jobFiles.length == 0) {
            System.out.println("No job files found in " + jobsDir.getAbsolutePath());
            // 如果没有文件，使用硬编码数据作为后备
            loadJobsFromHardcoded();
            return;
        }
        
        // 读取每个JSON文件并转换为Job对象
        for (File jobFile : jobFiles) {
            try (Reader reader = new FileReader(jobFile)) {
                Job job = gson.fromJson(reader, Job.class);
                
                // 检查职位是否有效且未删除
                if (isValidAndOpenJob(job)) {
                    jobs.add(job);
                    System.out.println("Loaded job from file: " + jobFile.getName());
                }
            } catch (IOException e) {
                System.err.println("Error reading job file " + jobFile.getName() + ": " + e.getMessage());
            }
        }
        
        // 按发布日期排序（最新的在前）
        jobs.sort((j1, j2) -> {
            String date1 = j1.getDates() != null ? j1.getDates().getPostedAt() : "";
            String date2 = j2.getDates() != null ? j2.getDates().getPostedAt() : "";
            return date2.compareTo(date1);
        });
        
        System.out.println("Total jobs loaded from files: " + jobs.size());
    }
    
    /**
     * 检查职位是否有效且处于开放状态
     */
    private boolean isValidAndOpenJob(Job job) {
        if (job == null || job.getJobId() == null) {
            return false;
        }
        
        // 检查是否已删除
        if (job.getLifecycle() != null && job.getLifecycle().isIsDeleted()) {
            return false;
        }
        
        // 检查发布状态
        if (job.getPublication() != null) {
            String pubStatus = job.getPublication().getStatus();
            if (pubStatus != null && !pubStatus.equalsIgnoreCase("published")) {
                return false;
            }
        }
        
        // 检查生命周期状态
        if (job.getLifecycle() != null) {
            String status = job.getLifecycle().getStatus();
            if (status == null || !status.equalsIgnoreCase("open")) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 后备方法：如果没有找到JSON文件，使用硬编码数据
     */
    private void loadJobsFromHardcoded() {

        // Job 1
        Job job1 = new Job();
        job1.setJobId("job_CS101_2026_spring");
        job1.setTitle("Introduction to Programming TA");

        Job.Course course1 = new Job.Course();
        course1.setCourseCode("CS 101");
        course1.setCourseName("Introduction to Programming");
        course1.setTerm("Spring");
        course1.setYear(2026);
        job1.setCourse(course1);

        job1.setDepartment("Computer Science");

        Job.Instructor instructor1 = new Job.Instructor();
        instructor1.setName("Dr. Sarah Johnson");
        instructor1.setEmail("sarah.johnson@university.edu");
        job1.setInstructor(instructor1);

        Job.Employment emp1 = new Job.Employment();
        emp1.setJobType("TA");
        emp1.setEmploymentType("Part-time TA");
        emp1.setWeeklyHours(10);
        emp1.setLocationMode("Hybrid");
        emp1.setLocationDetail("On-campus + Remote");
        job1.setEmployment(emp1);

        Job.Dates dates1 = new Job.Dates();
        dates1.setPostedAt("2026-03-17T10:00:00");
        dates1.setDeadline("2026-03-25T23:59:59");
        dates1.setStartDate("2026-04-01");
        dates1.setEndDate("2026-06-30");
        job1.setDates(dates1);

        Job.Content content1 = new Job.Content();
        content1.setSummary("Assist with lab sessions and grading assignments.");
        content1.setDescription("We are seeking a motivated and knowledgeable Teaching Assistant for Introduction to Programming.");
        content1.setResponsibilities(Arrays.asList(
            "Lead weekly lab sessions",
            "Hold office hours",
            "Grade assignments and quizzes",
            "Answer student questions online"
        ));
        content1.setRequirements(Arrays.asList(
            "Completed CS101 with A- or above",
            "Strong Python skills",
            "Good communication skills"
        ));
        content1.setPreferredSkills(Arrays.asList("Python", "Teaching Experience", "Git/GitHub"));
        job1.setContent(content1);

        Job.Lifecycle lifecycle1 = new Job.Lifecycle();
        lifecycle1.setStatus("open");
        lifecycle1.setIsDeleted(false);
        job1.setLifecycle(lifecycle1);

        jobs.add(job1);

        // Job 2
        Job job2 = new Job();
        job2.setJobId("job_CS201_2026_spring");
        job2.setTitle("Data Structures TA");

        Job.Course course2 = new Job.Course();
        course2.setCourseCode("CS 201");
        course2.setCourseName("Data Structures and Algorithms");
        course2.setTerm("Spring");
        course2.setYear(2026);
        job2.setCourse(course2);

        job2.setDepartment("Computer Science");

        Job.Instructor instructor2 = new Job.Instructor();
        instructor2.setName("Prof. Michael Chen");
        instructor2.setEmail("michael.chen@university.edu");
        job2.setInstructor(instructor2);

        Job.Employment emp2 = new Job.Employment();
        emp2.setJobType("TA");
        emp2.setEmploymentType("Part-time TA");
        emp2.setWeeklyHours(15);
        emp2.setLocationMode("On-Campus");
        emp2.setLocationDetail("Computer Science Building");
        job2.setEmployment(emp2);

        Job.Dates dates2 = new Job.Dates();
        dates2.setPostedAt("2026-03-15T10:00:00");
        dates2.setDeadline("2026-03-30T23:59:59");
        dates2.setStartDate("2026-04-01");
        dates2.setEndDate("2026-06-30");
        job2.setDates(dates2);

        Job.Content content2 = new Job.Content();
        content2.setSummary("Help students understand complex data structures and algorithms.");
        content2.setDescription("We are looking for an experienced TA to help students master data structures concepts.");
        content2.setResponsibilities(Arrays.asList(
            "Conduct weekly discussion sessions",
            "Help with programming assignments",
            "Hold tutoring hours",
            "Assist with exam preparation"
        ));
        content2.setRequirements(Arrays.asList(
            "Completed CS201 with A or above",
            "Proficient in Java or C++",
            "Strong problem-solving skills"
        ));
        content2.setPreferredSkills(Arrays.asList("Java", "C++", "Algorithm Design", " tutoring experience"));
        job2.setContent(content2);

        Job.Lifecycle lifecycle2 = new Job.Lifecycle();
        lifecycle2.setStatus("open");
        lifecycle2.setIsDeleted(false);
        job2.setLifecycle(lifecycle2);

        jobs.add(job2);

        // Job 3
        Job job3 = new Job();
        job3.setJobId("job_MATH101_2026_spring");
        job3.setTitle("Calculus I Grading TA");

        Job.Course course3 = new Job.Course();
        course3.setCourseCode("MATH 101");
        course3.setCourseName("Calculus I");
        course3.setTerm("Spring");
        course3.setYear(2026);
        job3.setCourse(course3);

        job3.setDepartment("Mathematics");

        Job.Instructor instructor3 = new Job.Instructor();
        instructor3.setName("Dr. Emily Watson");
        instructor3.setEmail("emily.watson@university.edu");
        job3.setInstructor(instructor3);

        Job.Employment emp3 = new Job.Employment();
        emp3.setJobType("Grading TA");
        emp3.setEmploymentType("Part-time TA");
        emp3.setWeeklyHours(8);
        emp3.setLocationMode("Remote");
        emp3.setLocationDetail("Online grading");
        job3.setEmployment(emp3);

        Job.Dates dates3 = new Job.Dates();
        dates3.setPostedAt("2026-03-16T10:00:00");
        dates3.setDeadline("2026-04-05T23:59:59");
        dates3.setStartDate("2026-04-01");
        dates3.setEndDate("2026-06-30");
        job3.setDates(dates3);

        Job.Content content3 = new Job.Content();
        content3.setSummary("Grade homework and provide feedback to students.");
        content3.setDescription("Help students succeed in their first calculus course by providing timely grading and feedback.");
        content3.setResponsibilities(Arrays.asList(
            "Grade weekly homework assignments",
            "Provide constructive feedback",
            "Answer student questions via email",
            "Report common issues to instructor"
        ));
        content3.setRequirements(Arrays.asList(
            "Completed MATH101 with A- or above",
            "Strong mathematical background",
            "Attention to detail"
        ));
        content3.setPreferredSkills(Arrays.asList("Math", "LaTeX", "Online grading"));
        job3.setContent(content3);

        Job.Lifecycle lifecycle3 = new Job.Lifecycle();
        lifecycle3.setStatus("open");
        lifecycle3.setIsDeleted(false);
        job3.setLifecycle(lifecycle3);

        jobs.add(job3);

        // Job 4 — 与原型「5 条职位」一致
        Job job4 = new Job();
        job4.setJobId("job_PHYS101_2026_spring");
        job4.setTitle("General Physics I Lab TA");
        Job.Course course4 = new Job.Course();
        course4.setCourseCode("PHYS 101");
        course4.setCourseName("General Physics I");
        course4.setTerm("Spring");
        course4.setYear(2026);
        job4.setCourse(course4);
        job4.setDepartment("Physics");
        Job.Instructor instructor4 = new Job.Instructor();
        instructor4.setName("Dr. James Rivera");
        instructor4.setEmail("james.rivera@university.edu");
        job4.setInstructor(instructor4);
        Job.Employment emp4 = new Job.Employment();
        emp4.setJobType("Lab TA");
        emp4.setEmploymentType("Part-time TA");
        emp4.setWeeklyHours(12);
        emp4.setLocationMode("On-Campus");
        emp4.setLocationDetail("Physics Building Lab");
        job4.setEmployment(emp4);
        Job.Dates dates4 = new Job.Dates();
        dates4.setPostedAt("2026-03-14T09:00:00");
        dates4.setDeadline("2026-04-10T23:59:59");
        dates4.setStartDate("2026-04-01");
        dates4.setEndDate("2026-06-30");
        job4.setDates(dates4);
        Job.Content content4 = new Job.Content();
        content4.setSummary("Supervise introductory physics labs and assist students with experiments.");
        content4.setDescription("Support students in mechanics and thermodynamics lab sections.");
        content4.setResponsibilities(Arrays.asList("Run lab sessions", "Grade lab reports", "Hold office hours"));
        content4.setRequirements(Arrays.asList("Completed PHYS101 with B+ or above", "Strong lab skills"));
        content4.setPreferredSkills(Arrays.asList("Physics", "Teaching", "Safety training"));
        job4.setContent(content4);
        Job.Lifecycle lifecycle4 = new Job.Lifecycle();
        lifecycle4.setStatus("open");
        lifecycle4.setIsDeleted(false);
        job4.setLifecycle(lifecycle4);
        jobs.add(job4);

        // Job 5
        Job job5 = new Job();
        job5.setJobId("job_CHEM201_2026_spring");
        job5.setTitle("Organic Chemistry Discussion TA");
        Job.Course course5 = new Job.Course();
        course5.setCourseCode("CHEM 201");
        course5.setCourseName("Organic Chemistry");
        course5.setTerm("Spring");
        course5.setYear(2026);
        job5.setCourse(course5);
        job5.setDepartment("Chemistry");
        Job.Instructor instructor5 = new Job.Instructor();
        instructor5.setName("Prof. Anna Mueller");
        instructor5.setEmail("anna.mueller@university.edu");
        job5.setInstructor(instructor5);
        Job.Employment emp5 = new Job.Employment();
        emp5.setJobType("TA");
        emp5.setEmploymentType("Part-time TA");
        emp5.setWeeklyHours(10);
        emp5.setLocationMode("Hybrid");
        emp5.setLocationDetail("Lab + online forum");
        job5.setEmployment(emp5);
        Job.Dates dates5 = new Job.Dates();
        dates5.setPostedAt("2026-03-12T11:00:00");
        dates5.setDeadline("2026-04-08T23:59:59");
        dates5.setStartDate("2026-04-01");
        dates5.setEndDate("2026-06-30");
        job5.setDates(dates5);
        Job.Content content5 = new Job.Content();
        content5.setSummary("Lead weekly discussion sections and review problem sets.");
        content5.setDescription("Help students master organic chemistry concepts and mechanisms.");
        content5.setResponsibilities(Arrays.asList("Discussion sections", "Review sessions", "Grade quizzes"));
        content5.setRequirements(Arrays.asList("Completed CHEM201 with A- or above", "Strong communication"));
        content5.setPreferredSkills(Arrays.asList("Chemistry", "OChem", "Tutoring"));
        job5.setContent(content5);
        Job.Lifecycle lifecycle5 = new Job.Lifecycle();
        lifecycle5.setStatus("open");
        lifecycle5.setIsDeleted(false);
        job5.setLifecycle(lifecycle5);
        jobs.add(job5);
    }

    /**
     * 从data/applications文件夹加载所有申请JSON文件
     */
    private void loadApplications() {
        applications = new ArrayList<>();

        File appsDir = findApplicationsDirectory();

        if (!appsDir.exists() || !appsDir.isDirectory()) {
            System.out.println("Warning: Applications directory not found at " + appsDir.getAbsolutePath());
            return;
        }

        File[] appFiles = appsDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (appFiles == null || appFiles.length == 0) {
            System.out.println("No application files found in " + appsDir.getAbsolutePath());
            return;
        }

        for (File appFile : appFiles) {
            try (Reader reader = new FileReader(appFile)) {
                Application app = gson.fromJson(reader, Application.class);
                if (app != null && app.getApplicationId() != null && !isApplicationDeleted(app)) {
                    applications.add(app);
                    System.out.println("Loaded application from file: " + appFile.getName());
                }
            } catch (IOException e) {
                System.err.println("Error reading application file " + appFile.getName() + ": " + e.getMessage());
            }
        }

        applications.sort((a1, a2) -> {
            String d1 = a1.getMeta() != null ? a1.getMeta().getSubmittedAt() : "";
            String d2 = a2.getMeta() != null ? a2.getMeta().getSubmittedAt() : "";
            return d2.compareTo(d1);
        });

        // 更新 nextApplicationId 为最大ID+1
        int maxId = 0;
        for (Application app : applications) {
            String appId = app.getApplicationId();
            if (appId != null && appId.startsWith("app_")) {
                try {
                    int num = Integer.parseInt(appId.substring(4));
                    if (num > maxId) maxId = num;
                } catch (NumberFormatException ignored) {}
            }
        }
        nextApplicationId = maxId + 1;

        System.out.println("Total applications loaded from files: " + applications.size());
        System.out.println("Next application ID will be: " + nextApplicationId);
    }

    private boolean isApplicationDeleted(Application app) {
        return app.getMeta() != null && app.getMeta().isIsDeleted();
    }

    // Getters
    public TAUser getCurrentUser() {
        return currentUser;
    }

    public List<Job> getJobs() {
        return jobs;
    }

    public List<Job> getOpenJobs() {
        return jobs.stream()
            .filter(j -> "open".equals(j.getStatus()))
            .toList();
    }

    public List<Application> getApplications() {
        return applications;
    }

    public List<Application> getUserApplications() {
        return applications.stream()
            .filter(a -> currentUser.getUserId().equals(a.getUserId()))
            .toList();
    }

    public Job getJobById(String jobId) {
        return jobs.stream()
            .filter(j -> j.getJobId().equals(jobId))
            .findFirst()
            .orElse(null);
    }

    public Application getApplicationById(String applicationId) {
        return applications.stream()
            .filter(a -> a.getApplicationId().equals(applicationId))
            .findFirst()
            .orElse(null);
    }

    public void addApplication(Application app) {
        app.setApplicationId("app_" + String.format("%06d", nextApplicationId++));
        app.setStudentId(currentUser.getProfile().getStudentId());
        app.setUserId(currentUser.getUserId());
        
        Application.Status status = new Application.Status();
        status.setCurrent("pending");
        status.setLabel("Pending");
        status.setColor("yellow");
        status.setLastUpdated(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        app.setStatus(status);

        List<Application.TimelineEvent> timeline = new ArrayList<>();
        Application.TimelineEvent event = new Application.TimelineEvent();
        event.setTimelineId("tl_" + System.currentTimeMillis());
        event.setStepKey("submitted");
        event.setStepLabel("Application Submitted");
        event.setStatus("completed");
        event.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        event.setNote("Application submitted successfully.");
        timeline.add(event);
        app.setTimeline(timeline);

        Application.Meta meta = new Application.Meta();
        meta.setSubmittedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        meta.setUpdatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        meta.setIsDeleted(false);
        app.setMeta(meta);

        Application.Review review = new Application.Review();
        review.setStatusMessage("Your application has been submitted and is waiting for review.");
        review.setNextSteps("Please wait for further updates from the department.");
        app.setReview(review);

        applications.add(0, app);
        
        // 保存申请到JSON文件
        saveApplicationToFile(app);
        
        // Update user summary
        TAUser.ApplicationSummary summary = currentUser.getApplicationSummary();
        summary.setTotalApplications(summary.getTotalApplications() + 1);
        summary.setPending(summary.getPending() + 1);
    }

    public int countApplicationsByStatus(String status) {
        return (int) getUserApplications().stream()
            .filter(a -> status.equals(a.getStatus().getCurrent()))
            .count();
    }
}
