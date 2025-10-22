import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * CollegeFeedbackWeb.java
 * Single-file small feedback system:
 * - Admin: view all feedback, add/remove faculty & students
 * - Faculty: view all feedback and reply
 * - Student: submit feedback and view own feedback + reply
 *
 * Added features:
 *  - Save feedback to CSV file automatically (feedback.csv)
 *  - Student search option for admin
 *  - Feedback analytics page for admin with inline SVG bar chart
 *  - Login time tracking (last login shown in topbar)
 *  - Dark mode toggle (persisted in localStorage)
 *
 * Original behavior preserved.
 */
public class CollegeFeedbackWeb {

    // in-memory data
    private static final Map<String, String> users = new HashMap<>();        // id -> password
    private static final Map<String, String> userRoles = new HashMap<>();    // id -> role ("admin","faculty","student")
    private static final List<Map<String, String>> responses = new ArrayList<>(); // feedback entries

    // new: track last login times (id -> ISO time string)
    private static final Map<String, String> lastLoginTime = new HashMap<>();

    // file where we persist feedback automatically (CSV)
    private static final String FEEDBACK_CSV = "feedback.csv";

    public static void main(String[] args) throws Exception {
        // seed users
        users.put("admin", "admin123"); userRoles.put("admin", "admin");
        users.put("faculty", "fac1");       userRoles.put("faculty", "faculty");
        users.put("student", "stud1");      userRoles.put("student", "student");

        // ensure CSV file exists with header
        ensureCsvHeader();

        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/", new LoginHandler());
        server.createContext("/dashboard", new DashboardHandler());
        server.createContext("/feedback", new FeedbackHandler());
        server.createContext("/facultyReplies", new FacultyReplyHandler());
        server.createContext("/studentReplies", new StudentRepliesHandler());
        server.createContext("/adminUsers", new AdminUsersHandler());

        // added contexts
        server.createContext("/analytics", new AnalyticsHandler());
        server.createContext("/export", new ExportHandler());
        server.createContext("/searchStudent", new SearchStudentHandler());

        server.setExecutor(null);
        System.out.println("🚀 Server started at http://localhost:8000/");
        server.start();
    }

    // ----------------- HELPERS -----------------
    private static void send(HttpExchange ex, String html) throws IOException {
        byte[] resp = html.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
        ex.sendResponseHeaders(200, resp.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(resp); }
    }

    private static void redirect(HttpExchange ex, String location) throws IOException {
        ex.getResponseHeaders().add("Location", location);
        ex.sendResponseHeaders(302, -1);
        ex.close();
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String,String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) map.put(kv[0], kv[1]);
        }
        return map;
    }

    private static Map<String, String> parseForm(String body) {
        Map<String,String> map = new HashMap<>();
        if (body == null || body.isEmpty()) return map;
        for (String pair : body.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                String key = kv[0];
                String val = kv[1];
                try {
                    val = URLDecoder.decode(val, StandardCharsets.UTF_8.name());
                } catch (UnsupportedEncodingException e) { /* won't happen */ }
                map.put(key, val);
            }
        }
        return map;
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;");
    }

    // ----------------- LOGIN -----------------
    static class LoginHandler implements HttpHandler {
        @Override public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String,String> form = parseForm(body);
                String id = form.get("id");
                String pass = form.get("password");

                if (id != null && users.containsKey(id) && users.get(id).equals(pass)) {
                    String role = userRoles.getOrDefault(id, "student");
                    // record login time
                    String now = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
                    lastLoginTime.put(id, now);
                    // redirect to dashboard with role/id
                    redirect(exchange, "/dashboard?role=" + role + "&id=" + id);
                } else {
                    String html = simplePage("Login - Failed",
                        "<h3 style='color:#c82333;'>❌ Invalid ID or Password</h3>" +
                        "<a class='btn' href='/'>Try Again</a>");
                    send(exchange, html);
                }
            } else {
                // show login page
                StringBuilder sb = new StringBuilder();
                sb.append("<html><head><title>Login</title>");
                sb.append(commonStyles());
                sb.append(commonDarkModeScript());
                sb.append("</head><body>");
                sb.append(centerBoxStart("🔐 Login"));
                sb.append("<form method='POST'>");
                sb.append("<input name='id' placeholder='User ID' required><br>");
                sb.append("<input name='password' type='password' placeholder='Password' required><br>");
                sb.append("<button class='btn' type='submit'>Login</button>");
                sb.append("</form>");
                sb.append("<p style='font-size:12px;color:#444;margin-top:8px;'>Default accounts: admin/admin123, faculty/fac1, student/stud1</p>");
                sb.append(centerBoxEnd());
                sb.append("</body></html>");
                send(exchange, sb.toString());
            }
        }
    }

    // ----------------- DASHBOARD -----------------
    static class DashboardHandler implements HttpHandler {
        @Override public void handle(HttpExchange exchange) throws IOException {
            Map<String,String> q = parseQuery(exchange.getRequestURI().getQuery());
            String role = q.get("role");
            String id = q.get("id");

            if (role == null || id == null) {
                redirect(exchange, "/");
                return;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("<html><head><title>Dashboard</title>");
            sb.append(commonStyles());
            sb.append(commonDarkModeScript());
            sb.append("</head><body>");
            // include last login time in topBar
            String lastLogin = lastLoginTime.get(id);
            String userTitle = id;
            if (lastLogin != null) {
                // format to readable local time
                try {
                    Instant in = Instant.parse(lastLogin);
                    String fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                                    .withZone(ZoneId.systemDefault()).format(in);
                    userTitle = id + " (Last login: " + fmt + ")";
                } catch (Exception ignored) { /* leave as raw ISO */ }
            }
            sb.append(topBar("Dashboard", userTitle, role));

            sb.append("<div class='container'>");
            sb.append("<h2>Welcome, ").append(escapeHtml(id)).append(" — <small>").append(escapeHtml(role)).append("</small></h2>");

            if ("admin".equals(role)) {
                sb.append("<div class='grid'>");
                sb.append(cardLink("/adminUsers?id=admin", "Manage Users", "➕/🗑 Add or remove faculty & students"));
                sb.append(cardLink("/studentReplies?id=admin", "View All Feedback", "📊 Read-only table of all feedback"));
                sb.append(cardLink("/analytics?id=admin", "Analytics", "📈 Feedback summary & chart"));
                sb.append(cardLink("/export?id=admin", "Export CSV", "💾 Download feedback.csv"));
                sb.append("</div>");
            } else if ("faculty".equals(role)) {
                sb.append("<div class='grid'>");
                sb.append(cardLink("/facultyReplies?id=" + id, "Reply to Feedback", "✍ View & reply to student feedback"));
                sb.append(cardLink("/studentReplies?id=admin", "View All Feedback (read-only)", "📋 Read-only table"));
                sb.append("</div>");
            } else { // student
                sb.append("<div class='grid'>");
                sb.append(cardLink("/feedback?id=" + id, "Submit Feedback", "📝 Fill the survey"));
                sb.append(cardLink("/studentReplies?id=" + id, "My Feedback & Replies", "📩 View submissions and replies"));
                sb.append("</div>");
            }

            sb.append("<div style='text-align:center;margin-top:18px;'><a class='btn' href='/'>🔒 Logout</a></div>");
            sb.append("<div style='text-align:center;margin-top:8px;'><button class='btn' onclick='toggleDarkMode()'>Toggle Dark Mode</button></div>");

            sb.append("</div></body></html>");
            send(exchange, sb.toString());
        }
    }

    // ----------------- FEEDBACK (Student) -----------------
    static class FeedbackHandler implements HttpHandler {
        @Override public void handle(HttpExchange exchange) throws IOException {
            Map<String,String> q = parseQuery(exchange.getRequestURI().getQuery());
            String studentId = q.get("id");
            if (studentId == null) { redirect(exchange, "/"); return; }

            if ("POST".equals(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String,String> form = parseForm(body);
                // ensure studentId stored
                form.put("studentId", studentId);
                // default reply empty
                form.putIfAbsent("reply", "");
                synchronized (responses) {
                    responses.add(form);
                }
                // new: save to CSV immediately
                try {
                    appendFeedbackToCsv(form);
                } catch (Exception e) {
                    System.err.println("Failed to append feedback to CSV: " + e.getMessage());
                }

                String html = simplePage("Submitted",
                    "<h3 style='color:green;'>✅ Feedback Submitted</h3>" +
                    "<a class='btn' href='/studentReplies?id=" + studentId + "'>View My Feedback</a>");
                send(exchange, html);
            } else {
                // show form
                StringBuilder sb = new StringBuilder();
                sb.append("<html><head><title>Submit Feedback</title>");
                sb.append(commonStyles());
                sb.append(commonDarkModeScript());
                sb.append("</head><body>");
                sb.append(topBar("Student Feedback", studentId, "student"));
                sb.append("<div class='container'>");
                sb.append("<h3>📝 Student Feedback Survey</h3>");
                sb.append("<form method='POST' action='/feedback?id=").append(escapeHtml(studentId)).append("'>");

                sb.append("<label>Full Name</label>");
                sb.append("<input name='name' required>");
                sb.append("<label>Department</label>");
                sb.append("<input name='department' placeholder='e.g. Computer Science'>");
                sb.append("<label>Year</label>");
                sb.append("<input name='year' placeholder='e.g. 2nd Year'>");

                // 5-point style replaced with options earlier - keep simple selects
                String[] questions = {
                    "Teaching quality of the faculty is excellent.",
                    "Course content is well-structured and clear.",
                    "Faculty encourages student participation in class.",
                    "Practical sessions/labs are helpful.",
                    "Learning resources (books/online) are adequate.",
                    "Assignments and projects are relevant.",
                    "Faculty is approachable for doubts and guidance.",
                    "Evaluation and grading are fair.",
                    "Classroom environment is conducive to learning.",
                    "College provides extra/co-curricular activities."
                };
                for (int i = 0; i < questions.length; i++) {
                    sb.append("<label>").append((i+1)).append(". ").append(escapeHtml(questions[i])).append("</label>");
                    sb.append("<select name='q").append(i+1).append("'>")
                      .append("<option>Excellent</option><option>Good</option><option>Average</option><option>Poor</option>")
                      .append("</select>");
                }

                sb.append("<label>Suggestions</label>");
                sb.append("<textarea name='suggestions' rows='3'></textarea>");
                sb.append("<button class='btn' type='submit'>Submit Feedback</button>");
                sb.append("</form>");

                sb.append("<p style='font-size:12px;color:#666;margin-top:12px;'>Note: your Student ID is <b>").append(escapeHtml(studentId)).append("</b></p>");
                sb.append("</div></body></html>");
                send(exchange, sb.toString());
            }
        }
    }

    // ----------------- FACULTY: view & reply -----------------
    static class FacultyReplyHandler implements HttpHandler {
        @Override public void handle(HttpExchange exchange) throws IOException {
            Map<String,String> q = parseQuery(exchange.getRequestURI().getQuery());
            String facultyId = q.get("id");
            if (facultyId == null) { redirect(exchange, "/"); return; }

            if ("POST".equals(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String,String> form = parseForm(body);
                String idxStr = form.get("index");
                String replyText = form.getOrDefault("reply", "");
                try {
                    int idx = Integer.parseInt(idxStr);
                    synchronized (responses) {
                        if (idx >= 0 && idx < responses.size()) {
                            responses.get(idx).put("reply", replyText);
                        }
                    }
                } catch (NumberFormatException ignored) {}
                redirect(exchange, "/facultyReplies?id=" + facultyId);
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("<html><head><title>Faculty Replies</title>");
                sb.append(commonStyles());
                sb.append(commonDarkModeScript());
                sb.append("</head><body>");
                sb.append(topBar("Faculty Panel", facultyId, "faculty"));
                sb.append("<div class='container'>");
                sb.append("<h3>✍ All Student Feedback (Reply)</h3>");

                synchronized (responses) {
                    if (responses.isEmpty()) {
                        sb.append("<p>No feedback submitted yet.</p>");
                    } else {
                        int idx = 0;
                        for (Map<String,String> fb : responses) {
                            sb.append("<div class='card'>");
                            sb.append("<div style='display:flex;justify-content:space-between;align-items:center;'>");
                            sb.append("<div><b>").append(escapeHtml(fb.getOrDefault("name", "Unknown"))).append("</b>");
                            sb.append(" <span style='color:#666'>(ID: ").append(escapeHtml(fb.getOrDefault("studentId","-"))).append(")</span></div>");
                            sb.append("<div style='font-size:12px;color:#666;'>#").append(idx).append("</div>");
                            sb.append("</div>");

                            // show Q1..Q10 briefly (compact)
                            sb.append("<div style='margin-top:8px;font-size:13px;'>");
                            for (int i=1;i<=10;i++) {
                                String a = fb.getOrDefault("q"+i, "-");
                                sb.append("<div><b>Q").append(i).append(":</b> ").append(escapeHtml(a)).append("</div>");
                            }
                            sb.append("</div>");

                            sb.append("<div style='margin-top:8px;'><b>Suggestions:</b> ").append(escapeHtml(fb.getOrDefault("suggestions","-"))).append("</div>");

                            sb.append("<form method='POST' action='/facultyReplies?id=").append(escapeHtml(facultyId)).append("' style='margin-top:10px;'>");
                            sb.append("<input type='hidden' name='index' value='").append(idx).append("'>");
                            sb.append("<label>Reply (visible to student)</label>");
                            sb.append("<textarea name='reply' rows='3'>").append(escapeHtml(fb.getOrDefault("reply",""))).append("</textarea>");
                            sb.append("<button class='btn' type='submit'>Save Reply</button>");
                            sb.append("</form>");

                            sb.append("</div>"); // card
                            idx++;
                        }
                    }
                }

                sb.append("</div></body></html>");
                send(exchange, sb.toString());
            }
        }
    }

    // ----------------- STUDENT & ADMIN VIEW -----------------
    static class StudentRepliesHandler implements HttpHandler {
        @Override public void handle(HttpExchange exchange) throws IOException {
            Map<String,String> q = parseQuery(exchange.getRequestURI().getQuery());
            String id = q.get("id"); // if "admin" or null -> admin view
            StringBuilder sb = new StringBuilder();
            sb.append("<html><head><title>Feedback & Replies</title>");
            sb.append(commonStyles());
            sb.append(commonDarkModeScript());
            sb.append("</head><body>");
            sb.append(topBar("Feedback & Replies", (id == null ? "admin": id), (id == null ? "admin" : (userRoles.getOrDefault(id,"student")))));
            sb.append("<div class='container'>");
            sb.append("<h3>📩 Feedback & Replies</h3>");

            boolean hasFeedback = false;
            if (id == null || "admin".equals(id)) {
                // admin view - table of all
                synchronized (responses) {
                    if (!responses.isEmpty()) hasFeedback = true;
                    sb.append("<div style='overflow:auto;'>");
                    sb.append("<table style='width:100%;border-collapse:collapse;'>");
                    sb.append("<tr style='background:#667eea;color:#fff;'><th>#</th><th>Student ID</th><th>Name</th><th>Dept</th><th>Year</th>");
                    for (int i=1;i<=10;i++) sb.append("<th>Q").append(i).append("</th>");
                    sb.append("<th>Suggestions</th><th>Reply</th></tr>");

                    int idx = 1;
                    for (Map<String,String> fb : responses) {
                        sb.append("<tr>");
                        sb.append("<td>").append(idx++).append("</td>");
                        sb.append("<td>").append(escapeHtml(fb.getOrDefault("studentId","-"))).append("</td>");
                        sb.append("<td>").append(escapeHtml(fb.getOrDefault("name","-"))).append("</td>");
                        sb.append("<td>").append(escapeHtml(fb.getOrDefault("department","-"))).append("</td>");
                        sb.append("<td>").append(escapeHtml(fb.getOrDefault("year","-"))).append("</td>");
                        for (int i=1;i<=10;i++) sb.append("<td>").append(escapeHtml(fb.getOrDefault("q"+i,"-"))).append("</td>");
                        sb.append("<td>").append(escapeHtml(fb.getOrDefault("suggestions","-"))).append("</td>");
                        sb.append("<td>").append(escapeHtml(fb.getOrDefault("reply","-"))).append("</td>");
                        sb.append("</tr>");
                    }
                    sb.append("</table></div>");
                }
            } else {
                // student view - only their feedbacks
                synchronized (responses) {
                    int idx = 1;
                    for (Map<String,String> fb : responses) {
                        if (id.equals(fb.get("studentId"))) {
                            hasFeedback = true;
                            sb.append("<div class='card'>");
                            sb.append("<div><b>Feedback #").append(idx++).append("</b></div>");
                            for (int i=1;i<=10;i++) {
                                sb.append("<div><b>Q").append(i).append(":</b> ").append(escapeHtml(fb.getOrDefault("q"+i,"-"))).append("</div>");
                            }
                            sb.append("<div style='margin-top:6px;'><b>Suggestions:</b> ").append(escapeHtml(fb.getOrDefault("suggestions","-"))).append("</div>");
                            sb.append("<div style='margin-top:6px;'><b>Faculty Reply:</b> ").append(escapeHtml(fb.getOrDefault("reply","Not replied yet"))).append("</div>");
                            sb.append("</div>");
                        }
                    }
                }
            }

            if (!hasFeedback) {
                sb.append("<p style='text-align:center;color:#666;'>No feedback found.</p>");
            }

            // Back link
            String roleParam = (id == null ? "admin" : userRoles.getOrDefault(id, "student"));
            String idParam = (id == null ? "admin" : id);
            sb.append("<div style='text-align:center;margin-top:12px;'><a class='btn' href='/dashboard?role=").append(roleParam).append("&id=").append(idParam).append("'>⬅ Back to Dashboard</a></div>");
            sb.append("</div></body></html>");
            send(exchange, sb.toString());
        }
    }

    // ----------------- ADMIN: Add/Remove Users (with search box) -----------------
    static class AdminUsersHandler implements HttpHandler {
        @Override public void handle(HttpExchange exchange) throws IOException {
            Map<String,String> q = parseQuery(exchange.getRequestURI().getQuery());
            String adminId = q.get("id");
            if (adminId == null || !"admin".equals(userRoles.get(adminId))) { redirect(exchange, "/"); return; }

            if ("POST".equals(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String,String> form = parseForm(body);
                String action = form.get("action"); // addUser / removeUser
                if ("addUser".equals(action)) {
                    String newId = form.get("newId");
                    String newPass = form.get("newPass");
                    String newRole = form.get("newRole");
                    if (newId != null && newPass != null && newRole != null) {
                        users.put(newId, newPass);
                        userRoles.put(newId, newRole);
                    }
                } else if ("removeUser".equals(action)) {
                    String remId = form.get("remId");
                    if (remId != null && !remId.equals("admin")) { // don't remove admin
                        users.remove(remId);
                        userRoles.remove(remId);
                        // also optionally remove their feedback entries
                        synchronized (responses) {
                            responses.removeIf(fb -> remId.equals(fb.get("studentId")));
                        }
                    }
                }
                redirect(exchange, "/adminUsers?id=" + adminId);
                return;
            }

            // GET - show admin user-management page
            StringBuilder sb = new StringBuilder();
            sb.append("<html><head><title>Admin - Manage Users</title>");
            sb.append(commonStyles());
            sb.append(commonDarkModeScript());
            sb.append("</head><body>");
            sb.append(topBar("Admin - Manage Users", adminId, "admin"));
            sb.append("<div class='container'><h3>🛠 Manage Users</h3>");

            // Search box (new)
            sb.append("<div class='card'><h4>Search Student / User</h4>");
            sb.append("<form method='GET' action='/searchStudent?id=").append(escapeHtml(adminId)).append("'>");
            sb.append("<input name='q' placeholder='Student ID or Name keyword' style='width:70%;display:inline-block;'>");
            sb.append("<button class='btn' type='submit' style='display:inline-block;margin-left:8px;'>Search</button>");
            sb.append("</form></div>");

            // Add form
            sb.append("<div class='card'><h4>Add User</h4>");
            sb.append("<form method='POST' action='/adminUsers?id=").append(escapeHtml(adminId)).append("'>");
            sb.append("<input type='hidden' name='action' value='addUser'>");
            sb.append("<label>User ID</label><input name='newId' required>");
            sb.append("<label>Password</label><input name='newPass' required>");
            sb.append("<label>Role</label><select name='newRole'><option value='student'>student</option><option value='faculty'>faculty</option></select>");
            sb.append("<button class='btn' type='submit'>Add User</button>");
            sb.append("</form></div>");

            // Existing users table
            sb.append("<div class='card'><h4>Existing Users</h4>");
            sb.append("<table style='width:100%;border-collapse:collapse;'><tr style='background:#667eea;color:#fff;'><th>ID</th><th>Role</th><th>Last Login</th><th>Action</th></tr>");
            // copy keys to avoid ConcurrentModification
            List<String> ids = new ArrayList<>(userRoles.keySet());
            Collections.sort(ids);
            for (String uid : ids) {
                sb.append("<tr><td>").append(escapeHtml(uid)).append("</td><td>").append(escapeHtml(userRoles.get(uid))).append("</td>");
                String ll = lastLoginTime.get(uid);
                String llDisplay = (ll == null ? "-" : escapeHtml(ll));
                sb.append("<td>").append(llDisplay).append("</td>");
                sb.append("<td>");
                if (!uid.equals("admin")) {
                    sb.append("<form method='POST' style='display:inline' action='/adminUsers?id=").append(escapeHtml(adminId)).append("'>");
                    sb.append("<input type='hidden' name='action' value='removeUser'>");
                    sb.append("<input type='hidden' name='remId' value='").append(escapeHtml(uid)).append("'>");
                    sb.append("<button class='btn small' type='submit'>Remove</button>");
                    sb.append("</form>");
                } else {
                    sb.append("<span style='color:#666;font-size:12px;'>(protected)</span>");
                }
                sb.append("</td></tr>");
            }
            sb.append("</table></div>");

            sb.append("<div style='text-align:center;margin-top:12px;'><a class='btn' href='/dashboard?role=admin&id=admin'>⬅ Back to Dashboard</a></div>");
            sb.append("</div></body></html>");
            send(exchange, sb.toString());
        }
    }

    // ----------------- SEARCH STUDENT (new) -----------------
    static class SearchStudentHandler implements HttpHandler {
        @Override public void handle(HttpExchange exchange) throws IOException {
            Map<String,String> q = parseQuery(exchange.getRequestURI().getQuery());
            String adminId = q.get("id");
            if (adminId == null || !"admin".equals(userRoles.get(adminId))) { redirect(exchange, "/"); return; }
            String query = q.getOrDefault("q", "").trim().toLowerCase();

            StringBuilder sb = new StringBuilder();
            sb.append("<html><head><title>Search Results</title>");
            sb.append(commonStyles());
            sb.append(commonDarkModeScript());
            sb.append("</head><body>");
            sb.append(topBar("Search Students", adminId, "admin"));
            sb.append("<div class='container'><h3>Search: ").append(escapeHtml(query)).append("</h3>");

            if (query.isEmpty()) {
                sb.append("<p style='color:#666;'>No search query provided.</p>");
            } else {
                // search responses by studentId or student name substring
                List<Map<String,String>> matches = new ArrayList<>();
                synchronized (responses) {
                    for (Map<String,String> fb : responses) {
                        String sid = fb.getOrDefault("studentId","").toLowerCase();
                        String name = fb.getOrDefault("name","").toLowerCase();
                        if (sid.contains(query) || name.contains(query)) matches.add(fb);
                    }
                }
                if (matches.isEmpty()) {
                    sb.append("<p style='color:#666;'>No feedback found for query.</p>");
                } else {
                    int idx = 1;
                    for (Map<String,String> fb : matches) {
                        sb.append("<div class='card'>");
                        sb.append("<div><b>").append(idx++).append(". ").append(escapeHtml(fb.getOrDefault("name","-"))).append("</b>");
                        sb.append(" <span style='color:#666'>(ID: ").append(escapeHtml(fb.getOrDefault("studentId","-"))).append(")</span></div>");
                        for (int i=1;i<=10;i++) {
                            sb.append("<div><b>Q").append(i).append(":</b> ").append(escapeHtml(fb.getOrDefault("q"+i,"-"))).append("</div>");
                        }
                        sb.append("<div style='margin-top:6px;'><b>Suggestions:</b> ").append(escapeHtml(fb.getOrDefault("suggestions","-"))).append("</div>");
                        sb.append("<div style='margin-top:6px;'><b>Reply:</b> ").append(escapeHtml(fb.getOrDefault("reply","Not replied yet"))).append("</div>");
                        sb.append("</div>");
                    }
                }
            }

            sb.append("<div style='text-align:center;margin-top:12px;'><a class='btn' href='/adminUsers?id=admin'>⬅ Back</a></div>");
            sb.append("</div></body></html>");
            send(exchange, sb.toString());
        }
    }

    // ----------------- ANALYTICS (new) -----------------
    static class AnalyticsHandler implements HttpHandler {
        @Override public void handle(HttpExchange exchange) throws IOException {
            Map<String,String> q = parseQuery(exchange.getRequestURI().getQuery());
            String adminId = q.get("id");
            if (adminId == null || !"admin".equals(userRoles.get(adminId))) { redirect(exchange, "/"); return; }

            // compute numeric score per response (Excellent=4..Poor=1) and aggregate counts
            int excellent=0, good=0, average=0, poor=0;
            int totalResponses = 0;
            synchronized (responses) {
                for (Map<String,String> fb : responses) {
                    double sum=0; int cnt=0;
                    for (int i=1;i<=10;i++) {
                        String a = fb.get("q"+i);
                        int s = answerToScore(a);
                        sum += s; cnt++;
                    }
                    if (cnt==0) continue;
                    double avg = sum/cnt;
                    if (avg >= 3.5) excellent++;
                    else if (avg >= 2.5) good++;
                    else if (avg >= 1.5) average++;
                    else poor++;
                    totalResponses++;
                }
            }

            // prepare simple SVG bar chart
            int[] vals = {excellent, good, average, poor};
            String[] labels = {"Excellent", "Good", "Average", "Poor"};
            int max = 1;
            for (int v: vals) if (v>max) max=v;
            int width = 600, height = 300;
            int padding = 40;
            int chartW = width - padding*2;
            int chartH = height - padding*2;
            int barWidth = chartW / vals.length - 20;

            StringBuilder svg = new StringBuilder();
            svg.append("<svg width='"+width+"' height='"+height+"' viewBox='0 0 "+width+" "+height+"' xmlns='http://www.w3.org/2000/svg'>");
            svg.append("<rect width='100%' height='100%' fill='transparent'/>");
            for (int i=0;i<vals.length;i++) {
                int v = vals[i];
                int barH = (int) ((double)v / Math.max(1, max) * (chartH - 10));
                int x = padding + i*(barWidth+20);
                int y = padding + (chartH - barH);
                String color = "#667eea";
                svg.append("<rect x='"+x+"' y='"+y+"' width='"+barWidth+"' height='"+barH+"' rx='6' ry='6' fill='"+color+"'/>");
                svg.append("<text x='"+(x+barWidth/2)+"' y='"+(y-6)+"' font-size='12' text-anchor='middle' fill='#333'>"+v+"</text>");
                svg.append("<text x='"+(x+barWidth/2)+"' y='"+(padding+chartH+16)+"' font-size='12' text-anchor='middle' fill='#333'>"+labels[i]+"</text>");
            }
            svg.append("</svg>");

            StringBuilder sb = new StringBuilder();
            sb.append("<html><head><title>Analytics</title>");
            sb.append(commonStyles());
            sb.append(commonDarkModeScript());
            sb.append("</head><body>");
            sb.append(topBar("Analytics", adminId, "admin"));
            sb.append("<div class='container'><h3>📊 Feedback Distribution</h3>");
            sb.append("<div style='text-align:center;'>");
            sb.append(svg.toString());
            sb.append("</div>");

            sb.append("<div style='margin-top:12px;text-align:center;'>");
            sb.append("<p>Total responses: <b>").append(totalResponses).append("</b></p>");
            sb.append("<a class='btn' href='/export?id=admin'>Download CSV</a>");
            sb.append("<a class='btn' href='/dashboard?role=admin&id=admin'>⬅ Back</a>");
            sb.append("</div>");

            sb.append("</div></body></html>");
            send(exchange, sb.toString());
        }
    }

    // ----------------- EXPORT CSV (new) -----------------
    static class ExportHandler implements HttpHandler {
        @Override public void handle(HttpExchange exchange) throws IOException {
            Map<String,String> q = parseQuery(exchange.getRequestURI().getQuery());
            String id = q.get("id");
            if (id == null || !"admin".equals(userRoles.get(id))) { redirect(exchange, "/"); return; }

            // stream the CSV file as download
            File f = new File(FEEDBACK_CSV);
            if (!f.exists()) {
                String html = simplePage("Export", "<p>No CSV file present yet.</p><a class='btn' href='/adminUsers?id=admin'>Back</a>");
                send(exchange, html);
                return;
            }

            // send headers for download
            exchange.getResponseHeaders().add("Content-Type", "text/csv; charset=UTF-8");
            exchange.getResponseHeaders().add("Content-Disposition", "attachment; filename=\"feedback.csv\"");
            byte[] data = readFileBytes(f);
            exchange.sendResponseHeaders(200, data.length);
            try (OutputStream os = exchange.getResponseBody()) { os.write(data); }
        }
    }

    // ----------------- Common UI helpers -----------------
    private static String commonStyles() {
        // includes light and dark CSS variables and classes; dark mode toggled by JS adding class "dark-mode" on body
        return "<style>" +
               ":root{--bg:#eef2ff;--card:#fff;--text:#222;--muted:#666;--primary:#667eea}" +
               ".dark-mode{--bg:#0f1724;--card:#111827;--text:#e5e7eb;--muted:#9ca3af;--primary:#60a5fa}" +
               "body{font-family:Segoe UI,Roboto,Arial;background:linear-gradient(135deg,var(--bg),#e6f0ff);margin:0;padding:0;color:var(--text)}" +
               ".container{width:90%;max-width:1100px;margin:20px auto;padding:18px;background:var(--card);border-radius:10px;box-shadow:0 6px 18px rgba(0,0,0,0.06)}" +
               ".btn{display:inline-block;background:var(--primary);color:#fff;padding:10px 14px;border-radius:8px;text-decoration:none;border:none;cursor:pointer;margin:6px 4px;font-weight:600}" +
               ".btn.small{padding:6px 8px;font-size:12px}" +
               ".grid{display:flex;gap:12px;flex-wrap:wrap;margin:8px 0}" +
               ".card{background:var(--card);border:1px solid rgba(255,255,255,0.03);padding:12px;border-radius:8px;margin:10px 0}" +
               "input,select,textarea{width:100%;padding:8px;margin:6px 0;border:1px solid #ddd;border-radius:6px}" +
               "table{width:100%;border-collapse:collapse}table th,table td{border:1px solid #eee;padding:8px;text-align:left}" +
               ".topbar{background:var(--card);padding:10px 18px;border-bottom:1px solid #f0f0f0;display:flex;align-items:center;justify-content:space-between}" +
               ".logo{font-weight:700;color:var(--text)}" +
               "</style>";
    }

    private static String commonDarkModeScript() {
        // JS toggles a "dark-mode" class on body and stores choice in localStorage
        return "<script>" +
               "function toggleDarkMode(){ if(document.body.classList.contains('dark-mode')){ document.body.classList.remove('dark-mode'); localStorage.setItem('cfw_theme','light'); } else { document.body.classList.add('dark-mode'); localStorage.setItem('cfw_theme','dark'); } }" +
               "(function(){ try{ if(localStorage.getItem('cfw_theme')==='dark') document.body.classList.add('dark-mode'); } catch(e){} })();" +
               "</script>";
    }

    private static String centerBoxStart(String title) {
        return "<div style='display:flex;justify-content:center;align-items:center;height:100vh;'><div style='width:360px;background:var(--card);padding:20px;border-radius:10px;box-shadow:0 8px 30px rgba(0,0,0,0.08);text-align:center;'>" +
               "<h2 style='margin:6px 0;'>" + title + "</h2>";
    }

    private static String centerBoxEnd() { return "</div></div>"; }

    private static String topBar(String pageTitle, String userId, String role) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class='topbar'><div class='logo'>CollegeFeedback</div>");
        sb.append("<div style='font-size:14px;color:var(--text);'>").append(escapeHtml(pageTitle)).append("</div>");
        sb.append("<div style='font-size:13px;color:var(--muted);'>").append(escapeHtml(userId)).append(" • ").append(escapeHtml(role)).append("</div>");
        sb.append("</div>");
        return sb.toString();
    }

    private static String cardLink(String href, String title, String subtitle) {
        StringBuilder sb = new StringBuilder();
        sb.append("<a class='btn' href='").append(href).append("' style='display:block;text-align:left;padding:12px 16px;'>");
        sb.append("<div style='font-weight:700;'>").append(escapeHtml(title)).append("</div>");
        sb.append("<div style='font-size:12px;color:#fff;margin-top:6px;'>").append(escapeHtml(subtitle)).append("</div>");
        sb.append("</a>");
        return sb.toString();
    }

    private static String simplePage(String title, String bodyHtml) {
        return "<html><head><title>" + escapeHtml(title) + "</title>" + commonStyles() + commonDarkModeScript() + "</head><body>"
            + "<div class='container' style='text-align:center;'>" + bodyHtml + "</div></body></html>";
    }

    // ----------------- CSV Helpers -----------------
    private static void ensureCsvHeader() {
        File f = new File(FEEDBACK_CSV);
        if (!f.exists()) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(f, false))) {
                StringBuilder head = new StringBuilder();
                for (int i=1;i<=10;i++) head.append("q").append(i).append(",");
                head.append("name,studentId,department,year,suggestions,reply\n");
                bw.write(head.toString());
            } catch (IOException e) {
                System.err.println("Couldn't create CSV header: " + e.getMessage());
            }
        }
    }

    private static synchronized void appendFeedbackToCsv(Map<String,String> fb) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FEEDBACK_CSV, true))) {
            StringBuilder row = new StringBuilder();
            for (int i=1;i<=10;i++) {
                int s = answerToScore(fb.get("q"+i));
                row.append(s).append(",");
            }
            // escape double quotes in text fields
            String name = fb.getOrDefault("name","").replace("\"","\"\"");
            String studentId = fb.getOrDefault("studentId","");
            String dept = fb.getOrDefault("department","").replace("\"","\"\"");
            String year = fb.getOrDefault("year","").replace("\"","\"\"");
            String suggestions = fb.getOrDefault("suggestions","").replace("\"","\"\"");
            String reply = fb.getOrDefault("reply","").replace("\"","\"\"");
            row.append("\"").append(name).append("\",");
            row.append("\"").append(studentId).append("\",");
            row.append("\"").append(dept).append("\",");
            row.append("\"").append(year).append("\",");
            row.append("\"").append(suggestions).append("\",");
            row.append("\"").append(reply).append("\"\n");
            bw.write(row.toString());
        }
    }

    private static byte[] readFileBytes(File f) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); FileInputStream fis = new FileInputStream(f)) {
            byte[] buf = new byte[4096];
            int r;
            while ((r = fis.read(buf)) != -1) bos.write(buf, 0, r);
            return bos.toByteArray();
        }
    }

    // ----------------- Scoring helper -----------------
    private static int answerToScore(String ans) {
        if (ans == null) return 2; // neutral default
        switch (ans.toLowerCase()) {
            case "excellent": return 4;
            case "good": return 3;
            case "average": return 2;
            case "poor": return 1;
            default:
                if (ans.contains("excel")) return 4;
                if (ans.contains("good")) return 3;
                if (ans.contains("avg") || ans.contains("average")) return 2;
                return 2;
        }
    }
}
