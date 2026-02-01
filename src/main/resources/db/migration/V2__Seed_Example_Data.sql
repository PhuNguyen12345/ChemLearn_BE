-- =============================================
-- 1. TẠO USERS (Bảng cha)
-- Mật khẩu demo: 'password123' (Lưu ý: Thực tế phải Hash bằng BCrypt)
-- =============================================

-- 1.1 Admin
INSERT INTO users (id, username, email, password, full_name, role, avatar_url)
VALUES (1, 'admin', 'admin@chemlearn.com', 'password123', 'Quản Trị Viên', 'ADMIN', 'https://i.pravatar.cc/150?u=admin');

-- 1.2 Giáo viên (Thầy Phú, Cô Lan)
INSERT INTO users (id, username, email, password, full_name, role, avatar_url) VALUES
                                                                                   (2, 'thayphu', 'phu.nguyen@chemlearn.com', 'password123', 'Thầy Phú Hóa', 'TEACHER', 'https://i.pravatar.cc/150?u=phu'),
                                                                                   (3, 'colan', 'lan.tran@chemlearn.com', 'password123', 'Cô Lan', 'TEACHER', 'https://i.pravatar.cc/150?u=lan');

-- 1.3 Phụ huynh (Bác Hùng)
INSERT INTO users (id, username, email, password, full_name, role, avatar_url)
VALUES (4, 'bachung', 'hung.le@gmail.com', 'password123', 'Lê Văn Hùng', 'PARENT', 'https://i.pravatar.cc/150?u=hung');

-- 1.4 Học sinh (Em Nam, Em Linh)
INSERT INTO users (id, username, email, password, full_name, role, avatar_url) VALUES
                                                                                   (5, 'namhocgioi', 'nam.le@gmail.com', 'password123', 'Lê Hoài Nam', 'STUDENT', 'https://i.pravatar.cc/150?u=nam'),
                                                                                   (6, 'linhcute', 'linh.nguyen@gmail.com', 'password123', 'Nguyễn Thùy Linh', 'STUDENT', 'https://i.pravatar.cc/150?u=linh');

-- Reset lại bộ đếm ID của bảng users để các user mới không bị trùng ID cũ
ALTER TABLE users ALTER COLUMN id RESTART WITH 100;


-- =============================================
-- 2. TẠO THÔNG TIN CHI TIẾT (Bảng con)
-- =============================================

-- 2.1 Teachers
INSERT INTO teachers (user_id, bio, specialization, degree, workplace) VALUES
                                                                           (2, '10 năm kinh nghiệm dạy Hóa luyện thi.', 'Hóa Vô Cơ', 'Thạc sĩ', 'Trường THPT Chuyên KHTN'),
                                                                           (3, 'Yêu thích thí nghiệm thực tế.', 'Hóa Hữu Cơ', 'Cử nhân', 'Trường THCS Cầu Giấy');

-- 2.2 Parents
INSERT INTO parents (user_id, phone_number, job_title) VALUES
    (4, '0988123456', 'Kỹ sư xây dựng');

-- 2.3 Students
-- Nam là con của Bác Hùng (parent_id = 4)
INSERT INTO students (user_id, grade_level, total_points, current_streak, parent_id, school_name) VALUES
                                                                                                      (5, 8, 1500, 5, 4, 'THCS Ngôi Sao'),
                                                                                                      (6, 9, 200, 1, NULL, 'THCS Archimedes');


-- =============================================
-- 3. TẠO LỚP HỌC & GHI DANH
-- =============================================

INSERT INTO classes (id, name, grade_level, teacher_id) VALUES
                                                            (1, 'Hóa Học Vui Vẻ - Lớp 8', 8, 2), -- Lớp thầy Phú
                                                            (2, 'Luyện Thi Vào 10 Cấp Tốc', 9, 3); -- Lớp cô Lan

-- Ghi danh học sinh vào lớp
INSERT INTO class_enrollments (class_id, student_id) VALUES
                                                         (1, 5), -- Nam học lớp thầy Phú
                                                         (2, 6); -- Linh học lớp cô Lan


-- =============================================
-- 4. TẠO NỘI DUNG HỌC TẬP (Chương & Bài học)
-- =============================================

-- Chương 1: Chất - Nguyên tử - Phân tử
INSERT INTO chapters (id, title, description, grade_level, order_index) VALUES
    (1, 'Chương 1: Chất - Nguyên tử - Phân tử', 'Kiến thức nền tảng về cấu tạo vật chất', 8, 1);

-- Bài 1: Video lý thuyết
INSERT INTO lessons (id, chapter_id, title, content_type, video_url, duration_minutes, order_index) VALUES
    (1, 1, 'Bài 1: Nguyên tử là gì?', 'VIDEO', 'https://www.youtube.com/embed/example', 15, 1);

-- Bài 2: Virtual Lab (Thí nghiệm ảo)
INSERT INTO lessons (id, chapter_id, title, content_type, lab_config, duration_minutes, order_index) VALUES
    (2, 1, 'Thực hành: Dựng mô hình nguyên tử', 'VIRTUAL_LAB',
     '{
       "lab_type": "atom_builder",
       "elements": ["H", "He", "Li"],
       "allow_hint": true,
       "simulation_url": "https://phet.colorado.edu/sims/html/build-an-atom/latest/build-an-atom_vi.html"
     }',
     30, 2);

-- Bài 3: Lý thuyết dạng Text
INSERT INTO lessons (id, chapter_id, title, content_type, text_content, duration_minutes, order_index) VALUES
    (3, 1, 'Bài 2: Nguyên tố hóa học', 'TEXT',
     '<h1>Nguyên tố hóa học</h1><p>Là tập hợp những nguyên tử cùng loại...</p>',
     10, 3);


-- =============================================
-- 5. TẠO CÂU HỎI & ĐÁP ÁN (Cho Bài 1)
-- =============================================

-- Câu 1
INSERT INTO questions (id, lesson_id, content, explanation) VALUES
    (1, 1, 'Hạt nhân nguyên tử được cấu tạo bởi các hạt nào?', 'Proton và Neutron nằm trong hạt nhân, Electron quay xung quanh.');

INSERT INTO answers (question_id, content, is_correct) VALUES
                                                           (1, 'Proton và Electron', FALSE),
                                                           (1, 'Proton và Neutron', TRUE),
                                                           (1, 'Chỉ có Proton', FALSE),
                                                           (1, 'Neutron và Electron', FALSE);

-- Câu 2
INSERT INTO questions (id, lesson_id, content, explanation) VALUES
    (2, 1, 'Điện tích của hạt Electron là gì?', 'Electron mang điện tích âm (-).');

INSERT INTO answers (question_id, content, is_correct) VALUES
                                                           (2, 'Điện tích Dương (+)', FALSE),
                                                           (2, 'Không mang điện', FALSE),
                                                           (2, 'Điện tích Âm (-)', TRUE);


-- =============================================
-- 6. GIẢ LẬP TIẾN ĐỘ HỌC TẬP & KẾT QUẢ THI
-- =============================================

-- Nam đã hoàn thành bài 1
INSERT INTO lesson_progress (student_id, lesson_id, is_completed, is_locked) VALUES
    (5, 1, TRUE, FALSE);

-- Nam đang làm bài 2 (Lab)
INSERT INTO lesson_progress (student_id, lesson_id, is_completed, is_locked) VALUES
    (5, 2, FALSE, FALSE);

-- Nam làm bài kiểm tra bài 1 được 9 điểm
INSERT INTO quiz_attempts (student_id, lesson_id, score, passed) VALUES
    (5, 1, 9.0, TRUE);


-- =============================================
-- 7. TẠO HUY HIỆU (Gamification)
-- =============================================
INSERT INTO badges (id, name, description, icon_url) VALUES
                                                         (1, 'Nhà Hóa Học Trẻ', 'Hoàn thành chương đầu tiên', 'assets/badges/young_chemist.png'),
                                                         (2, 'Chuyên Gia Thí Nghiệm', 'Hoàn thành 5 bài Virtual Lab', 'assets/badges/lab_expert.png');

-- Trao huy hiệu cho Nam
INSERT INTO user_badges (student_id, badge_id) VALUES (5, 1);