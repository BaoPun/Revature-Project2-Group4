package com.project2.demo.entities;

import java.net.URI;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.project2.demo.DAO.DBRepo;
import com.project2.demo.beans.Answer;
import com.project2.demo.beans.NewQuiz;
import com.project2.demo.beans.Progress;
import com.project2.demo.beans.Question;
import com.project2.demo.beans.Quiz;
import com.project2.demo.beans.Timetable;
import com.project2.demo.beans.User;
import com.project2.demo.beans.UserType;
import com.project2.demo.util.Password;



public class Engine {
	
	private Map<String,User> loggedInUsers;
	
	@Autowired
	private DBRepo services;

	
	public Engine() {
		super();
		loggedInUsers = new HashMap<String,User>();
	}
	
	// Logging out of the user, remove the mapping
	public void removeLoggedUser(String sessionID) {
		if(loggedInUsers.size() > 0)
			loggedInUsers.remove(sessionID);
	}
	
	
	// Is the current logged in user a teacher?
	public boolean isTeacherLoggedIn(String sessionID) {
		return (this.loggedInUsers.get(sessionID).getRole() == UserType.TEACHER ? true : false);
	}
	
	public List<User> getAllUsers() {
		return services.getAllUsers();
	}
	
	public List<User> getAllTeachers() {
		return services.getAllTeachers();
	}
	
	public List<Quiz> getQuizzesFromUser(User user) {
		return services.getQuizzesFromUser(user.getId());
	}
	
	public List<Quiz> getQuizzesStartedByStudent(int studentid) {
		return services.getQuizzesStartedByStudent(studentid);
	}
	
	public User getUserByName(String username) {
		return services.getUser(username);
	}
  
	public List<Progress> getProgressForUserAndQuiz(int quizid,int userid) {
		return services.getProgressForUserAndQuiz(quizid,userid);
	}
	
	public Question getQuestion(int questionid) {
		return services.getQuestion(questionid);
	}
	
	public Timetable getTimetable(int quizID, int userID) {
		return services.getTimetables(quizID, userID).get(0);
	}
	
	public int startQuiz(int quizID, int userID) {
		Timetable table = new Timetable();
		table.setQuizStartTime(Timestamp.valueOf(LocalDateTime.now()));
		table.setQuizID(quizID);
		table.setUserID(userID);
		return services.addTimetable(table);
	}
	
	public Timetable endQuiz(int quizID, int userID) {
		Timetable table = this.getTimetable(quizID, userID);
		if (table == null) {
			return null;
		}
		table.setQuizEndTime(Timestamp.valueOf(LocalDateTime.now()));
		services.updateTimetable(table);
		return table;
	}
	
	public Quiz makeNewQuiz(String sessionid, NewQuiz newQuiz) {
		User currentUser = loggedInUsers.get(sessionid);
		Quiz quiz = new Quiz();
		quiz.setName(newQuiz.getName());
		quiz.setUser(currentUser);
		services.addQuiz(quiz);
		
		List<String> questionStrs = newQuiz.getQuestions();
		List<List<String>> answerStrs = newQuiz.getAnswers();
		List<List<Boolean>> answerCorrectness = newQuiz.getAnswerCorrectness();
		for (int i=0;i<questionStrs.size();++i) {
			Question question = new Question();
			question.setDescription(questionStrs.get(i));
			question.setOrdering(i);
			question.setQuestionType("MULTIPLE CHOICE");
			question.setQuiz(quiz);
			services.addQuestion(question);
			
			for (int j=0;j<answerStrs.get(i).size();++j) {
				Answer answer = new Answer();
				answer.setAnswerText(answerStrs.get(i).get(j));
				answer.setIsCorrect(answerCorrectness.get(i).get(j));
				answer.setOrdering(j);
				answer.setQuestion(question);
				services.addAnswer(answer);
			}
		}
		
		return quiz;
	}
	
	public List<Question> getQuizQuestions(int quizid) {
		return services.getQuizQuestions(quizid);
	}
	
	public List<Answer> getQuestionAnswers(int questionid) {
		return services.getQuestionAnswers(questionid);
	}
	
	
	public boolean login(String sessionID, String username, String password) {
		User user = services.getUser(username);
		if (user == null) 
			return false;
		String hash = Password.hash(password);
		boolean matches=user.getPasswordHash().equals(hash);
		if (matches) 
			loggedInUsers.put(sessionID, user);
		return matches;
	}
	
	public boolean register(String username, String password, int teacherId) {
		User newStudent = new User(username, Password.hash(password), UserType.STUDENT, services.getUser(teacherId));
		boolean success = services.addUser(newStudent) > -1;
		if(success)
			System.out.println("Successfully added student: " + newStudent);
		else
			System.out.println("FAILURE");
		return success;
	}
	
	public User getLoggedInUser(String sessionID) {
		return loggedInUsers.get(sessionID);
	}
	
	public boolean isSessionLoggedIn(String sessionID) {
		return loggedInUsers.containsKey(sessionID);
	}
	
	public List<User> getMyStudents(String sessionID) {
		User getTeacher = loggedInUsers.get(sessionID);
		int teacherID = Integer.MIN_VALUE;
		if(getTeacher != null) {
			teacherID = getTeacher.getId();
			return getUserStudents(teacherID);
		}
		return null;
	}
	
	public List<User> getUserStudents(int teacherID) {
		return services.getUserStudents(teacherID);
	}
	
	public boolean isPermittedPage(String sessionID, URI uri) {
		User user = loggedInUsers.get(sessionID);
		String uriStr = uri.normalize().toASCIIString();
		
		if (user == null) {
			return !uriStr.matches("/s/.*");
		}
		switch (user.getRole()) {
			case ADMIN:
				return true;
			case STUDENT:
				return isStudentPage(uriStr);
			case TEACHER:
				return isTeacherPage(uriStr);
		}
		
		return true;
	}

	public boolean isStudentPage(String uri) {
		if (uri.matches("/s/teacher.html.*")) {
			return false;
		}
		if (uri.matches("/s/fragments.html.*")) {
			return false;
		}
		return true;
	}

	public boolean isTeacherPage(String uri) {
		return true;
	}
	
}


