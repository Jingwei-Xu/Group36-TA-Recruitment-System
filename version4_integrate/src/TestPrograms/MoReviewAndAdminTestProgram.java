package TestPrograms;

import Admin_Module.com.taapp.data.DataStore;
import Admin_Module.com.taapp.data.JsonDataRepository;
import Admin_Module.com.taapp.data.TaUserRepository;
import Admin_Module.com.taapp.model.Statistics;
import Admin_Module.com.taapp.model.TA;
import Admin_Module.com.taapp.service.AIWorkloadAnalysisService;
import MO_system.DataRoot;
import MO_system.MoContext;
import MO_system.model.review.ApplicationItem;
import MO_system.model.review.ReviewDashboardMetrics;
import MO_system.repository.JobRepository;
import MO_system.service.ApplicationReviewDataService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Integration tests for the MO review service and the Admin data layer.
 *
 * Avoids any real network traffic to external AI endpoints: the
 * {@link AIWorkloadAnalysisService} test only verifies safe instantiation,
 * never calls {@code analyze()}.
 */
public final class MoReviewAndAdminTestProgram {

    private MoReviewAndAdminTestProgram() {
    }

    public static void run() {
        System.out.println("---- MoReviewAndAdminTestProgram ----");

        // ---- MO review integration tests ----
        TestSupport.runTest("testApplicationReviewDataServiceLoadsData",
                MoReviewAndAdminTestProgram::testApplicationReviewDataServiceLoadsData);
        TestSupport.runTest("testBuildDashboardMetricsDoesNotCrash",
                MoReviewAndAdminTestProgram::testBuildDashboardMetricsDoesNotCrash);
        TestSupport.runTest("testBuildReviewSummaryMetricsDoesNotCrash",
                MoReviewAndAdminTestProgram::testBuildReviewSummaryMetricsDoesNotCrash);
        TestSupport.runTest("testFilterApplicationsByKeywordDoesNotCrash",
                MoReviewAndAdminTestProgram::testFilterApplicationsByKeywordDoesNotCrash);
        TestSupport.runTest("testFilterApplicationsByStatusDoesNotCrash",
                MoReviewAndAdminTestProgram::testFilterApplicationsByStatusDoesNotCrash);
        TestSupport.runTest("testNormalizeStatusForMetrics",
                MoReviewAndAdminTestProgram::testNormalizeStatusForMetrics);
        TestSupport.runTest("testJobRepositoryLoadMoJobIds",
                MoReviewAndAdminTestProgram::testJobRepositoryLoadMoJobIds);

        // ---- Admin data tests ----
        TestSupport.runTest("testAdminRepositoryCanLoadData",
                MoReviewAndAdminTestProgram::testAdminRepositoryCanLoadData);
        TestSupport.runTest("testTaUserRepositoryCanLoadData",
                MoReviewAndAdminTestProgram::testTaUserRepositoryCanLoadData);
        TestSupport.runTest("testStatisticsCanBeLoadedOrBuilt",
                MoReviewAndAdminTestProgram::testStatisticsCanBeLoadedOrBuilt);
        TestSupport.runTest("testAIWorkloadAnalysisServiceSafeInstantiation",
                MoReviewAndAdminTestProgram::testAIWorkloadAnalysisServiceSafeInstantiation);
    }

    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "true");
        TestSupport.reset();
        run();
        TestSupport.printSummary();
        System.exit(TestSupport.failed > 0 ? 1 : 0);
    }

    // ===================================================================
    // MO review integration tests
    // ===================================================================

    // Integration test: ApplicationReviewDataService can load real data for MO.
    private static void testApplicationReviewDataServiceLoadsData() {
        ApplicationReviewDataService svc = new ApplicationReviewDataService(DataRoot.resolve());
        JobRepository jobs = new JobRepository();
        Set<String> moJobIds = jobs.loadMoJobIds(MoContext.U_MO_001);

        // Allowed to be empty if no applications are mapped, but must not throw.
        List<ApplicationItem> items = svc.loadApplicationsForMo(MoContext.U_MO_001, moJobIds, null);
        TestSupport.assertNotNull(items, "loadApplicationsForMo must not return null");
    }

    // Integration test: dashboard metrics builder works on an empty list.
    private static void testBuildDashboardMetricsDoesNotCrash() {
        ApplicationReviewDataService svc = new ApplicationReviewDataService(DataRoot.resolve());
        ReviewDashboardMetrics m = svc.buildDashboardMetrics(0, new ArrayList<>());
        TestSupport.assertNotNull(m, "buildDashboardMetrics must not return null");
        TestSupport.assertEquals(0, m.totalApplications(), "empty list => 0 total");
        TestSupport.assertEquals(0, m.pendingReviews(), "empty list => 0 pending");
        TestSupport.assertEquals(0, m.approvedCount(), "empty list => 0 approved");
        TestSupport.assertEquals(0, m.rejectedCount(), "empty list => 0 rejected");
    }

    // Integration test: review-summary metrics is a 0-managed-jobs variant.
    private static void testBuildReviewSummaryMetricsDoesNotCrash() {
        ApplicationReviewDataService svc = new ApplicationReviewDataService(DataRoot.resolve());
        ReviewDashboardMetrics m = svc.buildReviewSummaryMetrics(syntheticItems());
        TestSupport.assertNotNull(m, "buildReviewSummaryMetrics must not return null");
        TestSupport.assertEquals(0, m.managedJobs(), "summary metrics managedJobs = 0");
        TestSupport.assertEquals(3, m.totalApplications(), "summary metrics total = list size");
        TestSupport.assertEquals(1, m.pendingReviews(), "1 pending");
        TestSupport.assertEquals(1, m.approvedCount(), "1 approved (accepted normalises to approved)");
        TestSupport.assertEquals(1, m.rejectedCount(), "1 rejected");
    }

    // Integration test: keyword filter must not crash on empty / non-empty inputs.
    private static void testFilterApplicationsByKeywordDoesNotCrash() {
        ApplicationReviewDataService svc = new ApplicationReviewDataService(DataRoot.resolve());
        List<ApplicationItem> all = syntheticItems();
        List<ApplicationItem> empty = svc.filterApplications(all, "no-such-keyword", null, null);
        TestSupport.assertNotNull(empty, "filterApplications must not return null");
        TestSupport.assertEquals(0, empty.size(), "Unmatched keyword filter must yield empty list");
    }

    // Integration + partition test: status filter (pending / approved / rejected).
    private static void testFilterApplicationsByStatusDoesNotCrash() {
        ApplicationReviewDataService svc = new ApplicationReviewDataService(DataRoot.resolve());
        List<ApplicationItem> all = syntheticItems();
        List<ApplicationItem> pending = svc.filterApplications(all, null, null, "pending");
        TestSupport.assertEquals(1, pending.size(), "pending filter must keep 1 item");
        List<ApplicationItem> approved = svc.filterApplications(all, null, null, "approved");
        TestSupport.assertEquals(1, approved.size(), "approved filter must keep 1 item");
        List<ApplicationItem> rejected = svc.filterApplications(all, null, null, "rejected");
        TestSupport.assertEquals(1, rejected.size(), "rejected filter must keep 1 item");
    }

    // White-box test: normalizeStatusForMetrics maps each known status.
    private static void testNormalizeStatusForMetrics() {
        TestSupport.assertEquals("pending",
                ApplicationReviewDataService.normalizeStatusForMetrics(itemWithStatus("pending")),
                "'pending' stays 'pending'");
        TestSupport.assertEquals("pending",
                ApplicationReviewDataService.normalizeStatusForMetrics(itemWithStatus("under_review")),
                "'under_review' maps to 'pending'");
        TestSupport.assertEquals("approved",
                ApplicationReviewDataService.normalizeStatusForMetrics(itemWithStatus("accepted")),
                "'accepted' maps to 'approved'");
        TestSupport.assertEquals("approved",
                ApplicationReviewDataService.normalizeStatusForMetrics(itemWithStatus("approved")),
                "'approved' stays 'approved'");
        TestSupport.assertEquals("rejected",
                ApplicationReviewDataService.normalizeStatusForMetrics(itemWithStatus("rejected")),
                "'rejected' stays 'rejected'");
        TestSupport.assertEquals("offer_pending",
                ApplicationReviewDataService.normalizeStatusForMetrics(itemWithStatus("offer_pending")),
                "'offer_pending' stays 'offer_pending'");
        // Blank + null status fall back to "pending".
        TestSupport.assertEquals("pending",
                ApplicationReviewDataService.normalizeStatusForMetrics(itemWithStatus("")),
                "blank status falls back to pending (no review)");
    }

    // Integration test: MO job index loads (may be empty if not configured).
    private static void testJobRepositoryLoadMoJobIds() {
        JobRepository jobs = new JobRepository();
        Set<String> ids = jobs.loadMoJobIds(MoContext.U_MO_001);
        TestSupport.assertNotNull(ids, "loadMoJobIds must not return null");
    }

    // ===================================================================
    // Admin data tests
    // ===================================================================

    // Integration test: Admin TaUserRepository can search by key.
    private static void testTaUserRepositoryCanLoadData() {
        TaUserRepository repo = new TaUserRepository();
        // Use a known login id from data/users/ta/user_ta_20230001.json
        Map<String, Object> result = repo.findByKey("johnsmith");
        TestSupport.assertNotNull(result, "findByKey must not return null");
        // Empty map is allowed if the dataset has changed; only assert
        // non-null contract and that the result is a Map.
    }

    // Integration test: Admin JsonDataRepository loads TAs and applications.
    private static void testAdminRepositoryCanLoadData() {
        JsonDataRepository repo = new JsonDataRepository();
        List<TA> tas = repo.loadTAs();
        TestSupport.assertNotNull(tas, "loadTAs must not return null");
        TestSupport.assertNotNull(repo.loadPositions(), "loadPositions must not return null");
        TestSupport.assertNotNull(repo.loadApplications(), "loadApplications must not return null");
    }

    // Integration test: Statistics can be loaded or computed from the dataset.
    private static void testStatisticsCanBeLoadedOrBuilt() {
        Statistics s = DataStore.defaultStore().getStatistics();
        TestSupport.assertNotNull(s, "Statistics must not be null");
        TestSupport.assertTrue(s.getTotalApplications() >= 0, "total applications must be >= 0");
        TestSupport.assertTrue(s.getTotalTAs() >= 0, "total TAs must be >= 0");
    }

    // Safety test: AIWorkloadAnalysisService should be safely constructible
    // without triggering an external API call. The actual analyze() method
    // would issue an HTTP request to an external AI endpoint; calling it
    // here would violate the test policy of no real network usage, so
    // we only verify safe instantiation and skip the network branch.
    private static void testAIWorkloadAnalysisServiceSafeInstantiation() {
        AIWorkloadAnalysisService svc = new AIWorkloadAnalysisService();
        TestSupport.assertNotNull(svc, "AIWorkloadAnalysisService must instantiate");
        // Intentionally not calling svc.analyze() — would issue external HTTP.
    }

    // ===================================================================
    // Synthetic fixtures (no I/O)
    // ===================================================================

    private static List<ApplicationItem> syntheticItems() {
        List<ApplicationItem> list = new ArrayList<>();
        list.add(itemWithStatus("pending"));
        list.add(itemWithStatus("accepted"));
        list.add(itemWithStatus("rejected"));
        return list;
    }

    private static ApplicationItem itemWithStatus(String current) {
        ApplicationItem item = new ApplicationItem();
        ApplicationItem.Status st = new ApplicationItem.Status();
        st.setCurrent(current);
        item.setStatus(st);
        return item;
    }
}
