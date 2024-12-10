// Проект з Java - Табала Матвій, 3-ій курс, "Комп'ютерна математика", 2-га група
// Останння зміна файлу: 10.12.2024, 18:06:23

import java.io.*;
import java.sql.*;
import java.util.*;

@SuppressWarnings("unused")
public class StudentManagementSystem {
    /*
     * Створіть систему для обліку студентів, яка дозволяє зберігати та редагувати персональні дані студентів та їх успішність.
     * Система дозволяє визначати успішність індивідуально та для кожної групи та курсу, а також по відношенню до викладача.
     * Всі дані зберігаються в SQL сумісній БД та можуть конвертуватись та завантажуватись в/з CSV. 
    */

    // Клас для викладача
    static class Teacher {
        /*
         * Клас для викладача, який містить ID, ім'я та предмет, який він викладає.
         * Клас містить конструктор та методи доступу до полів.
        */
        private int id;
        private String name;
        private String subject;

        public Teacher(int id, String name, String subject) { // Конструктор
            this.id = id;
            this.name = name;
            this.subject = subject;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getSubject() { return subject; }
    }

    // Клас для студента
    static class Student {
        /*
         * Клас для студента, який містить ID, ім'я, прізвище, ID групи та ID курсу.
         * Клас містить конструктор та методи доступу до полів.
        */
        private int id;
        private String firstName;
        private String lastName;
        private int groupId;
        private int courseId;

        public Student(int id, String firstName, String lastName, int groupId, int courseId) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.groupId = groupId;
            this.courseId = courseId;
        }

        public int getId() { return id; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public int getGroupId() { return groupId; }
        public int getCourseId() { return courseId; }
    }

    // Клас для оцінок
    static class Grades {
        /*
         * Клас для оцінок, який містить ID студента, ID викладача та оцінку.
         * Клас містить конструктор та методи доступу до полів.
        */
        private int studentId;
        private int teacherId;
        private double grade;

        public Grades(int studentId, int teacherId, double grade) {
            this.studentId = studentId;
            this.teacherId = teacherId;
            this.grade = grade;
        }

        public int getStudentId() { return studentId; }
        public int getTeacherId() { return teacherId; }
        public double getGrade() { return grade; }
    }

    public static void main(String[] args) {
        /*
        !  Основний метод програми, який викликається при запуску програми.
         * Метод створює файл final_grades.csv, якщо його немає, та підключається до SQLite БД.
         * Метод створює таблиці в БД, додає викладачів, завантажує дані з CSV та виводить меню для користувача.
         * Метод обробляє вибір користувача та викликає відповідні методи для додавання, виведення, редагування, конвертації та завантаження даних.
        */
        try {

            // Перевірка наявності файлу ./resources/final_grades.csv та створення, якщо його немає
            File csvFile = new File("./resources/final_grades.csv");
            if (!csvFile.exists()) { // Якщо файл не існує, створюємо його і записуємо заголовок
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile))) {
                    writer.write("Student Grades:");
                    writer.newLine();
                    writer.write("FirstName,LastName,CourseId,GroupId,AverageGrade,TeacherId");
                    writer.newLine();
                    System.out.println("File final_grades.csv has been created.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            // Підключення до SQLite БД у файлі ./resources/completedb.db
            Connection connection = DriverManager.getConnection("jdbc:sqlite:./resources/completedb.db");
            DatabaseDAO databaseDAO = new DatabaseDAO(connection);

            // Створення таблиць в БД
            databaseDAO.createTables();

            // Додавання викладачів у БД
            List<Teacher> teachers = createTeachers();
            addTeachersToDatabase(databaseDAO, teachers);

            // Завантаження студентів з CSV
            loadAllDataFromCSV(databaseDAO, "final_grades");

            try (Scanner scanner = new Scanner(System.in)) {
                while (true) {
                    System.out.println("Choose an option:");
                    System.out.println("1. Add a student");
                    System.out.println("2. Display all students");
                    System.out.println("3. Edit a student");
                    System.out.println("4. Convert and save data to CSV");
                    System.out.println("5. Import data from CSV");
                    System.out.println("6. Exit");
                    int choice = scanner.nextInt();
                    scanner.nextLine(); // Очищаємо буфер після вводу числа

                    if (choice == 1) {
                        // Додавання студента
                        System.out.println("Enter student's first name:");
                        String firstName = scanner.nextLine();
                        System.out.println("Enter student's last name:");
                        String lastName = scanner.nextLine();
                        System.out.println("Enter student's course:");
                        int courseId = scanner.nextInt();
                        System.out.println("Enter student's group:");
                        int groupId = scanner.nextInt();
                        scanner.nextLine();

                        System.out.println("Enter student's grade:");
                        double grade = scanner.nextDouble();
                        scanner.nextLine();

                        // Обираємо викладача
                        System.out.println("Select teacher by ID:");
                        for (Teacher teacher : teachers) { // Змінна teacher з типом Teacher як елемент списку teachers
                            System.out.println(teacher.getId() + ". " + teacher.getName() + " - " + teacher.getSubject());
                        }
                        int teacherId = scanner.nextInt();
                        scanner.nextLine(); // Очищаємо буфер після вводу числа

                        // Перевірка, чи є студент в базі
                        Student student = databaseDAO.getStudentByNameAndGroup(firstName, lastName, groupId, courseId);
                        if (student == null) {
                            // Якщо студента немає, додаємо його
                            student = new Student(0, firstName, lastName, groupId, courseId);
                            databaseDAO.addStudent(student);
                        }

                        // Додавання оцінки
                        databaseDAO.addGrade(student.getId(), teacherId, grade);
                        System.out.println("Grade has been added to the student.");

                    } else if (choice == 2) {
                        // Виведення всіх студентів з обчисленням середнього
                        List<Student> students = databaseDAO.getAllStudents();
                        for (Student s : students) {
                            double avgGrade = databaseDAO.getAverageGradeForStudent(s.getId()); // Отримання середнього бал
                            System.out.println("Student: " + s.getFirstName() + " " + s.getLastName() +
                                    ", Course: " + s.getCourseId() + ", Group: " + s.getGroupId() + 
                                    ", Average grade: " + Math.ceil(avgGrade * 100) / 100 + ", " + "ID: " + s.getId());
                        }
                    } else if (choice == 3) {
                        // Редагування студента
                        System.out.println("Enter student's ID to edit:");
                        int studentId = scanner.nextInt();
                        scanner.nextLine();

                        Student student = databaseDAO.getStudentById(studentId);
                        if (student == null) {
                            System.out.println("Student not found.");
                            continue;
                        }

                        System.out.println("Select field to edit:");
                        System.out.println("1. First Name");
                        System.out.println("2. Last Name");
                        System.out.println("3. Course");
                        System.out.println("4. Group");
                        System.out.println("5. Grade");
                        System.out.println("6. Teacher");
                        int editChoice = scanner.nextInt();
                        scanner.nextLine();

                        switch (editChoice) {
                            /* Вибір поля для редагування:
                             * 1. Ім'я
                             * 2. Прізвище
                             * 3. Курс
                             * 4. Група
                             * 5. Оцінка
                             * 6. Викладач
                            */
                            case 1:
                                System.out.println("Enter new first name:");
                                String newFirstName = scanner.nextLine();
                                databaseDAO.updateStudentFirstName(studentId, newFirstName);
                                System.out.println("First name has been updated.");
                                break;
                            case 2:
                                System.out.println("Enter new last name:");
                                String newLastName = scanner.nextLine();
                                databaseDAO.updateStudentLastName(studentId, newLastName);
                                System.out.println("Last name has been updated.");
                                break;
                            case 3:
                                System.out.println("Enter new course:");
                                int newCourseId = scanner.nextInt();
                                scanner.nextLine();
                                databaseDAO.updateStudentCourseId(studentId, newCourseId);
                                System.out.println("Course has been updated.");
                                break;
                            case 4:
                                System.out.println("Enter new group:");
                                int newGroupId = scanner.nextInt();
                                scanner.nextLine();
                                databaseDAO.updateStudentGroupId(studentId, newGroupId);
                                System.out.println("Group has been updated.");
                                break;
                            case 5:
                                System.out.println("Enter new grade:");
                                double newGrade = scanner.nextDouble();
                                scanner.nextLine();
                                databaseDAO.updateGrade(studentId, newGrade);
                                System.out.println("Grade has been updated.");
                                break;
                            case 6:
                                System.out.println("Select new teacher by ID:");
                                for (Teacher teacher : teachers) {
                                    System.out.println(teacher.getId() + ". " + teacher.getName() + " - " + teacher.getSubject());
                                }
                                int updatedTeacherId = scanner.nextInt();
                                scanner.nextLine();
                                databaseDAO.updateTeacherForStudent(studentId, updatedTeacherId);
                                System.out.println("Teacher updated.");
                                break;
                            default:
                                System.out.println("Invalid choice.");
                        }

                    } else if (choice == 4) {
                        // Збереження даних у CSV
                        saveDataToCSV(databaseDAO);
                        System.out.println("Data has been saved to final_grades.csv.");
                    } else if (choice == 5) {
                        // Завантаження даних з CSV
                        System.out.println("Enter the CSV filename (without .csv):");
                        String fileName = scanner.nextLine();
                        loadAllDataFromCSV(databaseDAO, fileName);
                        System.out.println("Data has been loaded from " + fileName + ".csv.");
                    } else if (choice == 6) {
                        break;
                    } else {
                        System.out.println("Invalid choice. Try again.");
                    }
                }
            }

            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** 
     * @param databaseDAO - об'єкт класу DatabaseDAO
     * @param fileName - ім'я файлу CSV
     * @return Метод для завантаження усіх даних з CSV файлу.
     * Метод очищує всі таблиці в БД та завантажує дані з CSV.
     * Метод також зберігає оновлені дані у файл final_grades.csv.
     */
    public static void loadAllDataFromCSV(DatabaseDAO databaseDAO, String fileName) {
        String csvFilePath = "./resources/" + fileName + ".csv";
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {
            String line; // Читання файлу по рядках
            boolean isStudentSection = false; // Перевірка, чи це секція студентів
            boolean isAllGradesSection = false; // Перевірка, чи це секція всіх оцінок
            Map<Integer, Integer> studentIdMap = new HashMap<>(); // Мапити старий ID студента на новий ID

            // Видалення всіх даних з таблиць для завантаження нових даних
            Statement stmt = databaseDAO.connection.createStatement();
            stmt.execute("DELETE FROM Students");
            stmt.execute("DELETE FROM Grades");
            stmt.execute("DELETE FROM sqlite_sequence WHERE name='Students'");
            stmt.execute("DELETE FROM sqlite_sequence WHERE name='Grades'");
            System.out.println("All data has been cleared.");

            while ((line = reader.readLine()) != null) {
                line = line.trim(); // Видалення пробілів з початку та кінця рядка

                if (line.equalsIgnoreCase("Student Grades:")) {
                    isStudentSection = true;
                    isAllGradesSection = false;
                    // Пропускаємо рядок заголовка по типу: "FirstName,LastName,CourseId,GroupId,AverageGrade,TeacherId"
                    reader.readLine();
                    continue;
                } else if (line.equalsIgnoreCase("All Grades:")) {
                    isStudentSection = false;
                    isAllGradesSection = true;
                    // Аналогічно: "StudentId,TeacherId,Grade"
                    reader.readLine();
                    continue;
                } else if (line.startsWith("-----") || line.isEmpty()) {
                    continue; // Пропускаємо роздільники та порожні рядки
                }

                if (isStudentSection) {
                    // Парсинг даних студента
                    String[] tokens = line.split(",");
                    if (tokens.length >= 6) { // Перевірка, чи є всі необхідні дані
                        int oldStudentId = Integer.parseInt(tokens[0].trim()); // Старий ID студента
                        String firstName = tokens[1].trim(); // Ім'я
                        String lastName = tokens[2].trim(); // Прізвище
                        int courseId = Integer.parseInt(tokens[3].trim()); // ID курсу
                        int groupId = Integer.parseInt(tokens[4].trim()); // ID групи

                        // Додаваємо студента до бази даних
                        Student student = new Student(0, firstName, lastName, groupId, courseId);
                        databaseDAO.addStudent(student);

                        // Зберігаємо маппінг старого та нового ID студента
                        studentIdMap.put(oldStudentId, student.getId());
                    }
                } else if (isAllGradesSection) {
                    // Парсимо всі оцінки
                    String[] tokens = line.split(","); // Розділяємо рядок по комі
                    if (tokens.length >= 3) {
                        int oldStudentId = Integer.parseInt(tokens[0].trim());
                        int teacherId = Integer.parseInt(tokens[1].trim());
                        double grade = Double.parseDouble(tokens[2].trim());

                        Integer newStudentId = studentIdMap.get(oldStudentId); // Отримуємо новий ID студента
                        if (newStudentId != null) {
                            // Додавання оцінки до бази даних
                            databaseDAO.addGrade(newStudentId, teacherId, grade);
                        }
                    }
                }
            }
            System.out.println("Data has been loaded from" + csvFilePath);

            // Збереження оновлених даних у файл final_grades.csv
            saveDataToCSV(databaseDAO);
            System.out.println("final_grades.csv has been updated.");
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    
    /** 
     * @param databaseDAO - об'єкт класу DatabaseDAO
     * @return Метод для збереження даних у CSV файл.
     * Метод записує студентів, групи, курси, викладачів та оцінки у файл final_grades.csv з відповідним форматом.
     */
    public static void saveDataToCSV(DatabaseDAO databaseDAO) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("./resources/final_grades.csv"))) {
            // Запис студентських оцінок
            writer.write("Student Grades:");
            writer.newLine();
            writer.write("StudentId, FirstName, LastName, CourseId, GroupId, AverageGrade");
            writer.newLine();
            List<Student> students = databaseDAO.getAllStudents(); // Декларуємо список студентів і заповнюємо його
            for (Student student : students) {
                double avgGrade = databaseDAO.getAverageGradeForStudent(student.getId());
                writer.write(student.getId() + ", " + student.getFirstName() + ", " + student.getLastName() + ", " +
                        student.getCourseId() + ", " + student.getGroupId() + ", " + Math.ceil(avgGrade * 100) / 100);
                writer.newLine();
            }

            // Обчислення та запис успішності груп
            Map<String, List<Double>> groupGrades = new HashMap<>(); // Створюємо відображення для зберігання середніх оцінок груп
            for (Student student : students) {
                double avgGrade = databaseDAO.getAverageGradeForStudent(student.getId());
                String groupKey = student.getGroupId() + "," + student.getCourseId();
                groupGrades.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(avgGrade); // Якщо ключ не асоційований зі значенням, то додаємо його
            }

            writer.newLine();
            writer.write("-------------------------------------------------\n");
            writer.newLine();
            writer.write("Group Performances:");
            writer.newLine();
            writer.write("GroupId, CourseId, AverageGrade");
            writer.newLine();
            for (Map.Entry<String, List<Double>> entry : groupGrades.entrySet()) { // для кожної циклу ітеруємось між ключами та значеннями в мапі groupGrades
                String[] keys = entry.getKey().split(",");
                int groupId = Integer.parseInt(keys[0]);
                int courseId = Integer.parseInt(keys[1]);
                List<Double> grades = entry.getValue(); // Отримуємо список оцінок
                double sum = 0;
                for (Double grade : grades) {
                    sum += grade; // Сумуємо оцінки
                }
                double avgGrade = grades.size() > 0 ? sum / grades.size() : 0; // Обчислюємо середню оцінку
                writer.write(groupId + ", " + courseId + ", " + Math.ceil(avgGrade * 100) / 100); // Записуємо дані у файл
                writer.newLine();
            }

            // Обчислення та запис успішності курсів
            Map<Integer, List<Double>> courseGrades = new HashMap<>(); // Створюємо відображення для зберігання середніх оцінок курсів
            for (Student student : students) {
                int courseId = student.getCourseId();
                double avgGrade = databaseDAO.getAverageGradeForStudent(student.getId());
                courseGrades.computeIfAbsent(courseId, k -> new ArrayList<>()).add(avgGrade);
            }

            writer.newLine();
            writer.write("-------------------------------------------------\n");
            writer.newLine();
            writer.write("Course Performances:");
            writer.newLine();
            writer.write("CourseId, AverageGrade");
            writer.newLine();
            for (Map.Entry<Integer, List<Double>> entry : courseGrades.entrySet()) {
                int courseId = entry.getKey();
                List<Double> grades = entry.getValue();
                double sum = 0;
                for (Double grade : grades) {
                    sum += grade;
                }
                double avgGrade = grades.size() > 0 ? sum / grades.size() : 0; // Обчислюємо середню оцінку, враховуючи, що список може бути порожнім
                writer.write(courseId + ", " + Math.ceil(avgGrade * 100) / 100);
                writer.newLine();
            }

            // Запис успішності викладачів
            writer.newLine();
            writer.write("-------------------------------------------------\n");
            writer.newLine();
            writer.write("Teacher Performances:");
            writer.newLine();
            List<Teacher> teachers = databaseDAO.getAllTeachers();
            for (Teacher teacher : teachers) {
                writer.write("Teacher's full name: " + teacher.getName());
                writer.newLine();
                writer.write("Student's full name, Student's course, Student's group, Student's average grade");
                writer.newLine();
                // StudentGrade - це вкладений клас у класі DatabaseDAO, який є частиною зовнішнього класу StudentManagementSystem
                List<StudentManagementSystem.DatabaseDAO.StudentGrade> studentGrades = databaseDAO.getStudentsByTeacher(teacher.getId());
                for (StudentManagementSystem.DatabaseDAO.StudentGrade sg : studentGrades) {
                    writer.write(sg.firstName + " " + sg.lastName + ", " + sg.courseId + ", " +
                            sg.groupId + ", " + Math.ceil(sg.averageGrade * 100) / 100);
                    writer.newLine();
                }
                writer.newLine();

                // Додавання успішності груп та курсів для викладача
                writer.write("Group Performances for Course " + teacher.getSubject() + ":");
                writer.newLine();
                Map<String, List<Double>> teacherGroupGrades = new HashMap<>();
                for (StudentManagementSystem.DatabaseDAO.StudentGrade sgGrade : studentGrades) {
                    String groupKey = sgGrade.groupId + "," + sgGrade.courseId;
                    teacherGroupGrades.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(sgGrade.averageGrade);
                }
                writer.write("GroupId, CourseId, AverageGrade");
                writer.newLine();
                for (Map.Entry<String, List<Double>> entryGroup : teacherGroupGrades.entrySet()) {
                    String[] keys = entryGroup.getKey().split(",");
                    int groupId = Integer.parseInt(keys[0]);
                    int courseId = Integer.parseInt(keys[1]);
                    List<Double> grades = entryGroup.getValue();
                    double sum = 0;
                    for (Double grade : grades) {
                        sum += grade;
                    }
                    double avgGrade = grades.size() > 0 ? sum / grades.size() : 0;
                    writer.write(groupId + ", " + courseId + ", " + Math.ceil(avgGrade * 100) / 100);
                    writer.newLine();
                }

                writer.newLine();
                writer.write("Course Performances:");
                writer.newLine();
                Map<Integer, List<Double>> teacherCourseGrades = new HashMap<>();
                for (StudentManagementSystem.DatabaseDAO.StudentGrade sgGrade : studentGrades) {
                    int courseId = sgGrade.courseId;
                    // Якщо ключ не асоційований зі значенням, то додаємо його
                    teacherCourseGrades.computeIfAbsent(courseId, k -> new ArrayList<>()).add(sgGrade.averageGrade);
                }
                writer.write("CourseId, AverageGrade");
                writer.newLine();
                for (Map.Entry<Integer, List<Double>> entryCourse : teacherCourseGrades.entrySet()) {
                    int courseId = entryCourse.getKey();
                    List<Double> grades = entryCourse.getValue();
                    double sum = 0;
                    for (Double grade : grades) {
                        sum += grade;
                    }
                    double avgGrade = grades.size() > 0 ? sum / grades.size() : 0;
                    writer.write(courseId + ", " + Math.ceil(avgGrade * 100) / 100);
                    writer.newLine();
                }
                writer.newLine();
                writer.write("-------------------------------------------------\n");
                writer.newLine();
            }

            // Запис усіх оцінок
            writer.write("All Grades:");
            writer.newLine();
            writer.write("StudentId, TeacherId, Grade");
            writer.newLine();
            List<Grades> grades = databaseDAO.getAllGrades();
            for (Grades grade : grades) {
                writer.write(grade.getStudentId() + ", " + grade.getTeacherId() + ", " + grade.getGrade());
                writer.newLine();
            }
            
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return Метод створює список викладачів та додає їх до списку.
     */
    public static List<Teacher> createTeachers() {
        List<Teacher> teachers = new ArrayList<>();
        teachers.add(new Teacher(1, "Ivan Andriyovych", "Mathematical Analysis"));
        teachers.add(new Teacher(2, "Maria Ivanivna", "Probability Theory"));
        teachers.add(new Teacher(3, "Maksym Vitaliyovych", "Discrete Mathematics"));
        teachers.add(new Teacher(4, "Serhiy Vasylovych", "Programming"));
        return teachers;
    }

    
    /** 
     * @param databaseDAO - об'єкт класу DatabaseDAO
     * @param teachers - список викладачів
     * @return Метод додає кожного викладача зі списку викладачів до бази даних.
     * @throws SQLException - виняток, який виникає при помилці SQL
     */
    public static void addTeachersToDatabase(DatabaseDAO databaseDAO, List<Teacher> teachers) throws SQLException {
        for (Teacher teacher : teachers) {
            databaseDAO.addTeacher(teacher);
        }
    }

    // Оновлений клас DatabaseDAO для роботи з викладачами та оцінками
    public static class DatabaseDAO {
        /*
         * Клас для роботи з базою даних.
         * Клас містить методи для створення таблиць, додавання викладачів, студентів та оцінок, отримання даних та оновлення даних.
        */

        private Connection connection; // Підключення до БД

        public DatabaseDAO(Connection connection) {
            this.connection = connection; // Конструктор
        }

        // Допоміжний клас для зберігання оцінок студентів, бо клас Grades не містить інформацію про студента
        public static class StudentGrade {
            /*
             * Клас для зберігання оцінок студентів.
             * Клас містить ім'я, прізвище, ID курсу, ID групи та середню оцінку.
            */
            String firstName;
            String lastName;
            int courseId;
            int groupId;
            double averageGrade;

            public StudentGrade(String firstName, String lastName, int courseId, int groupId, double averageGrade, int teacherId) {
                this.firstName = firstName;
                this.lastName = lastName;
                this.courseId = courseId;
                this.groupId = groupId;
                this.averageGrade = averageGrade;
            }
        }

        /**
         * @return Метод виконує запит до БД та створює таблиці Students, Teachers та Grades.
         * @throws SQLException
         */
        public void createTables() throws SQLException {
            String studentsTable = "CREATE TABLE IF NOT EXISTS Students (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "firstName TEXT," +
                    "lastName TEXT," +
                    "groupId INTEGER," +
                    "courseId INTEGER)";
            String teachersTable = "CREATE TABLE IF NOT EXISTS Teachers (" +
                    "id INTEGER PRIMARY KEY," +
                    "name TEXT," +
                    "subject TEXT)";
            String gradesTable = "CREATE TABLE IF NOT EXISTS Grades (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "studentId INTEGER," +
                    "teacherId INTEGER," +
                    "grade REAL," +
                    "FOREIGN KEY(studentId) REFERENCES Students(id)," +
                    "FOREIGN KEY(teacherId) REFERENCES Teachers(id))";
            Statement stmt = connection.createStatement();
            stmt.execute(studentsTable);
            stmt.execute(teachersTable);
            stmt.execute(gradesTable);
        }

        /**
         * @param teacher - об'єкт класу Teacher
         * @return Метод виконує запит до таблиці Teachers та додає викладача.
         * @throws SQLException
         */
        public void addTeacher(Teacher teacher) throws SQLException {
            String query = "INSERT OR IGNORE INTO Teachers (id, name, subject) VALUES (?, ?, ?)";
            PreparedStatement pstmt = connection.prepareStatement(query); // Підготовлений запит, краще працює з частими змінами значень
            pstmt.setInt(1, teacher.getId());
            pstmt.setString(2, teacher.getName());
            pstmt.setString(3, teacher.getSubject());
            pstmt.executeUpdate();
        }

        /**
         * @return повертає список усіх викладачів з таблиці Teachers
        */
        public List<Teacher> getAllTeachers() throws SQLException {
            List<Teacher> teachers = new ArrayList<>();
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Teachers");
            while (rs.next()) {
                teachers.add(new Teacher(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("subject")
                ));
            }
            return teachers;
        }


        /**
         * @param student
         * @return Метод виконує запит до таблиці Students та додає студента.
         * @throws SQLException
         */
        public void addStudent(Student student) throws SQLException {
            String query = "INSERT INTO Students (firstName, lastName, groupId, courseId) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, student.getFirstName());
            pstmt.setString(2, student.getLastName());
            pstmt.setInt(3, student.getGroupId());
            pstmt.setInt(4, student.getCourseId());
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                student.id = rs.getInt(1);
            }
        }

        /**
         * @param studentId - ID студента
         * @param teacherId - ID викладача
         * @param grade - оцінка
         * @return Метод виконує запит до таблиці Grades та додає оцінку студенту.
         * @throws SQLException
         */
        public void addGrade(int studentId, int teacherId, double grade) throws SQLException {
            String query = "INSERT INTO Grades (studentId, teacherId, grade) VALUES (?, ?, ?)";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, teacherId);
            pstmt.setDouble(3, grade);
            pstmt.executeUpdate();
        }

        /**
         * @param firstName - ім'я
         * @param lastName - прізвище
         * @param groupId - ID групи
         * @param courseId - ID курсу
         * @return Метод виконує запит до таблиці Students по ім'ям, прізвищу, групі та курсу та повертає студента.
         * @throws SQLException - виняток, який виникає при помилці SQL
         */
        public Student getStudentByNameAndGroup(String firstName, String lastName, int groupId, int courseId) throws SQLException {
            String query = "SELECT * FROM Students WHERE firstName = ? AND lastName = ? AND groupId = ? AND courseId = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            pstmt.setInt(3, groupId);
            pstmt.setInt(4, courseId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Student(
                        rs.getInt("id"),
                        rs.getString("firstName"),
                        rs.getString("lastName"),
                        rs.getInt("groupId"),
                        rs.getInt("courseId")
                );
            }
            return null;
        }

        /** 
         * @param studentId - ID студента
         * @return - повертає студента за ID
        */
        public Student getStudentById(int studentId) throws SQLException {
            String query = "SELECT * FROM Students WHERE id = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Student(
                        rs.getInt("id"),
                        rs.getString("firstName"),
                        rs.getString("lastName"),
                        rs.getInt("groupId"),
                        rs.getInt("courseId")
                );
            }
            return null;
        }

        /**
         * @return - повертає список усіх студентів
         * @throws SQLException
         */
        public List<Student> getAllStudents() throws SQLException {
            List<Student> students = new ArrayList<>();
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Students");
            while (rs.next()) {
                students.add(new Student(
                        rs.getInt("id"),
                        rs.getString("firstName"),
                        rs.getString("lastName"),
                        rs.getInt("groupId"),
                        rs.getInt("courseId")
                ));
            }
            return students;
        }

        /**
         * @param studentId - ID студента
         * @return Метод виконує запит до таблиці Grades та обчислює середню оцінку для студента.
         * @throws SQLException
         */
        public double getAverageGradeForStudent(int studentId) throws SQLException {
            String query = "SELECT AVG(grade) as avgGrade FROM Grades WHERE studentId = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("avgGrade");
            }
            return 0;
        }

        /***
         * @param teacherId
         * @return Метод виконує запит до таблиць Students та Grades та повертає список студентів викладача.
         * @throws SQLException
         */
        public List<StudentGrade> getStudentsByTeacher(int teacherId) throws SQLException {
            List<StudentGrade> studentGrades = new ArrayList<>();
            String query = "SELECT s.firstName, s.lastName, s.courseId, s.groupId, AVG(g.grade) as averageGrade " +
                            "FROM Students s " +
                            "JOIN Grades g ON s.id = g.studentId " +
                            "WHERE g.teacherId = ? " +
                            "GROUP BY s.id";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, teacherId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                studentGrades.add(new StudentGrade(
                        rs.getString("firstName"),
                        rs.getString("lastName"),
                        rs.getInt("courseId"),
                        rs.getInt("groupId"),
                        Math.ceil(rs.getDouble("averageGrade") * 100) / 100,
                        teacherId
                ));
            }
            return studentGrades;
        }

        /**
         * @param studentId - ID студента
         * @param newFirstName - нове ім'я
         * @return Метод виконує запит до таблиці Students та оновлює ім'я студента.
         * @throws SQLException
         */
        public void updateStudentFirstName(int studentId, String newFirstName) throws SQLException {
            String query = "UPDATE Students SET firstName = ? WHERE id = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, newFirstName);
            pstmt.setInt(2, studentId);
            pstmt.executeUpdate();
        }

        /**
         * @param studentId - ID студента
         * @param newLastName - нове прізвище
         * @return Метод виконує запит до таблиці Students та оновлює прізвище студента.
         * @throws SQLException
         */
        public void updateStudentLastName(int studentId, String newLastName) throws SQLException {
            String query = "UPDATE Students SET lastName = ? WHERE id = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, newLastName);
            pstmt.setInt(2, studentId);
            pstmt.executeUpdate();
        }

        /**
         * @param studentId - ID студента
         * @param newCourseId - новий ID курсу
         * @return Метод виконує запит до таблиці Students та оновлює курс студента.
         * @throws SQLException
         */
        public void updateStudentCourseId(int studentId, int newCourseId) throws SQLException {
            String query = "UPDATE Students SET courseId = ? WHERE id = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, newCourseId);
            pstmt.setInt(2, studentId);
            pstmt.executeUpdate();
        }

        /**
         * @param studentId - ID студента
         * @param newGroupId - новий ID групи
         * @return Метод виконує запит до таблиці Students та оновлює групу студента.
         * @throws SQLException
         */
        public void updateStudentGroupId(int studentId, int newGroupId) throws SQLException {
            String query = "UPDATE Students SET groupId = ? WHERE id = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, newGroupId);
            pstmt.setInt(2, studentId);
            pstmt.executeUpdate();
        }

        /**
         * @param studentId - ID студента
         * @param newGrade - нова оцінка
         * @return Метод виконує запит до таблиці Grades та оновлює оцінку студента.
         * @throws SQLException
         */
        public void updateGrade(int studentId, double newGrade) throws SQLException {
            String query = "UPDATE Grades SET grade = ? WHERE studentId = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setDouble(1, newGrade);
            pstmt.setInt(2, studentId);
            pstmt.executeUpdate();
        }

        /**
         * @param studentId - ID студента
         * @param newTeacherId - новий ID викладача
         * @return Метод виконує запит до таблиці Grades та оновлює викладача для студента.
         * @throws SQLException
         */
        public void updateTeacherForStudent(int studentId, int newTeacherId) throws SQLException {
            String query = "UPDATE Grades SET teacherId = ? WHERE studentId = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, newTeacherId);
            pstmt.setInt(2, studentId);
            pstmt.executeUpdate();
        }

        /**
         * @return Метод виконує запит до таблиці Grades та повертає всі оцінки.
        */
        public List<Grades> getAllGrades() throws SQLException {
            /*
             * Метод для отримання всіх оцінок з бази даних.
             * Метод виконує запит до таблиці Grades та повертає список оцінок.
            */
            String query = "SELECT * FROM Grades";
            PreparedStatement pstmt = connection.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            List<Grades> grades = new ArrayList<>();
            while (rs.next()) {
                grades.add(new Grades(
                        rs.getInt("studentId"),
                        rs.getInt("teacherId"),
                        rs.getDouble("grade")
                ));
            }
            return grades;
        }
    }
}
