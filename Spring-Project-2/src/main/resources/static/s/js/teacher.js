

$(function () {
	$('.student-list-item').on('click',studentListItemClick);
})

var progressData={};


function updateProgress() {
	let score=document.getElementById("quiz-score");
	let table=document.getElementById("quiz-table");
	let tbody=document.getElementById("quiz-body");

	let correctAnswers=0;
	for (i=0;i<progressData.length;++i) {
		if (progressData[i].answer.isCorrect) {
			++correctAnswers;
		}
	}
	let scorePercentage=100*correctAnswers*(1.0/progressData.length);
	score.innerText="Score: "+scorePercentage+"%";

	tbody.innerHTML="";
	for (i=0;i<progressData.length;++i) {
		let trElt=document.createElement("tr");
		let thQuestion=document.createElement("th");
		let thAnswer=document.createElement("th");
		thQuestion.setAttribute("scope","col");
		thAnswer.setAttribute("scope","col");
		if (progressData[i].answer.isCorrect) {
			thAnswer.classList.add("blue");
		} else {
			thAnswer.classList.add("red");
		}
		trElt.appendChild(thQuestion);
		trElt.appendChild(thAnswer);
		tbody.appendChild(trElt);
		trElt.onclick=rowClick;
		trElt.setAttribute("trid",i);
		thQuestion.innerText=i+1;
		thAnswer.innerText=progressData[i].answer.ordering+1;
	}
}

function rowClick() {
	let trid=this.getAttribute("trid");
	document.getElementById("rowClickModalTitle").innerText="Question #"+(parseInt(trid)+1);
	let questionLabel = document.getElementById("question-label");
	let answerList=document.getElementById("answer-list");
	let xhttp = new XMLHttpRequest();
	xhttp.onreadystatechange = function() {
		if (xhttp.readyState==4) {
			if (xhttp.status == 200) {
				questionData = JSON.parse(xhttp.responseText);
				questionLabel.innerText=questionData.description;
				answerList.innerHTML="";
				for (i=0;i<questionData.answers.length;++i) {
					let listitem=document.createElement("li");
					answerList.appendChild(listitem);
					listitem.innerText=questionData.answers[i].answerText;
					if (questionData.answers[i].isCorrect) {
						listitem.classList.add("bold");
					}
				}
				console.log(xhttp.responseText);
				$("#rowClickModal").modal();
			} else {
				console.log("error in rowclick");
			}
		}
	}
	xhttp.open("GET","/s/getSingleQuestion?questionid="+progressData[trid].answer.questionID);
	xhttp.send();
	
}

function updateScores() {
	let quizid=this.getAttribute("quizid");
	let xhttp = new XMLHttpRequest();
	xhttp.onreadystatechange = function () {
		if (this.readyState == 4) {
			if (this.status == 200) {
				progressData=JSON.parse(this.responseText)
				updateProgress();
			} else {
				console.log("error");
			}
		}
	}
	xhttp.open("GET","/s/getUserQuizResults?quiz="+quizid+"&user="+studentid);
	xhttp.send();
}

function updateQuizList() {
	let testsLabel = document.getElementById("tests-label");
	testsLabel.style.visibility="visible";

	let quizListGroup=document.getElementById("quizListGroup");
	quizListGroup.innerHTML="";
	for (let i=0;i<quizList.length;++i) {
		let div=document.createElement("div");
		div.classList.add("list-item");
		div.setAttribute("quizid",quizList[i]['id'])
		div.onclick=updateScores;
		let link=document.createElement("a");
		link.setAttribute("href","#");
		link.classList.add("list-group-item");
		link.classList.add("list-group-item-action");
		link.innerText=quizList[i]['name'];
		div.appendChild(link);
		quizListGroup.appendChild(div);
	}
}

var quizList=[];
var studentid="";

function studentListItemClick() {
	studentid=this.getAttribute("studentid");
	let xhttp = new XMLHttpRequest();
	xhttp.onreadystatechange = function () {
		if (this.readyState == 4) {
			if (this.status == 200) {
				quizList = JSON.parse(this.responseText);
				updateQuizList();
			} else {
				alert("error");
			}
		}
	};
	let uri = "/s/getQuizzesStartedByStudent?userid="+studentid;
	xhttp.open("GET", uri);
	xhttp.send();
}



let history = null

// Detect navigation arrows if we're already logged out but trying to get in via outside means.
window.addEventListener('DOMContentLoaded', () => {
    if(String(window.performance.getEntriesByType("navigation")[0].type) === "back_forward" && localStorage.getItem('closed') == 'normal'){
		alert('Error, you are not logged in.  You will now be redirected to the login page.')
		location.href = '/'
	}
	else if(localStorage.getItem('type') == 'STUDENT'){
		alert('Error, you are a student.  You will now be redirected to the Student page.')
		location.href = '/s/student'
	} 
	else if(localStorage.getItem('closed') == 'forced')
		history = localStorage.getItem('user')
})

// The moment the Teacher window loads, do stuff
window.addEventListener('load', () => {

	// First, check if the id field is set.
	let getFirstDiv = document.getElementsByTagName('div')[0]	
	history = getFirstDiv.getAttribute('id')
	console.log(history)

	// If not, redirect back to the login page.
	if(!getFirstDiv.hasAttribute('id')){
		alert('Error, you are not logged in.  You will now be redirected to the login page.')
		location.href = '/'
	}
	else{
		// Mark the logged in User as a teacher
		localStorage.setItem('type', 'TEACHER')

		// Change the name from Quiz Manager to "Teacher view"
		document.getElementsByClassName('navbar-brand')[0].textContent = `${history}`

		// Hide most items in the navbar, they're pointless
		document.getElementsByClassName('nav-item nav-link')[1].style.display = 'none'
		document.getElementsByClassName('nav-item nav-link')[2].style.display = 'none'

		// Reserve 1 item in the navbar for creating a brand new quiz
		document.getElementsByClassName('nav-item nav-link')[0].textContent = 'Create New Quiz'
	}
})

// Do stuff before logging out via the log out button
document.getElementsByClassName('nav-item nav-link')[3].addEventListener('click', () => {
	localStorage.setItem('closed', 'normal')
	localStorage.removeItem('type')
	alert(`Logging out, see you next time ${history}`)
})

// Do stuff before the page closes
window.addEventListener('beforeunload', () => {
	if(localStorage.getItem('closed') != null && localStorage.getItem('closed') != 'normal'){
		localStorage.setItem('closed', 'forced')
		localStorage.setItem('user', history)
	}
})