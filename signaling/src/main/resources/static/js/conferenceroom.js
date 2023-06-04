var ws = new WebSocket('wss://localhost:8059/socket');
ws.onopen = function () { console.log("connected"); }

var participants = {};
var name;

window.onbeforeunload = function() {
	ws.close();
};

// 웹소켓으로 메시지를 받았을 때
ws.onmessage = function(message) {
	var parsedMessage = JSON.parse(message.data);
	console.info('Received message: ' + message.data);

	switch (parsedMessage.id) {
		case 'existingParticipants':
			onExistingParticipants(parsedMessage);
			break;
		case 'newParticipantArrived':
			onNewParticipant(parsedMessage);
			break;
		case 'participantLeft':
			onParticipantLeft(parsedMessage);
			break;
		case 'receiveVideoAnswer':
			receiveVideoResponse(parsedMessage);
			break;
		case 'onIceCandidate':
			participants[parsedMessage.userId].rtcPeer.addIceCandidate(parsedMessage.candidate, function (error) {
				if (error) {
				  console.error("Error adding candidate: " + error);
				  alert(error);
				  return;
				}
			});
			break;

		case 'videoStateAnswer' :
			break;

		case 'audioStateAnswer' :
			break;

		case 'timerStateAnswer' :
			timerResponse(parsedMessage);
			break;

	default:
		console.error('Unrecognized message', parsedMessage);
	}
}

// join room 누르면 signaling 서버로 메시지 날라감
function register() {
	name = document.getElementById('name').value;
	var room = document.getElementById('roomId').value;

	document.getElementById('room-header').innerText = 'ROOM ' + room;
	document.getElementById('join').style.display = 'none';
	document.getElementById('room').style.display = 'block';

	var message = {
		id : 'joinRoom',
		userId : name,
		roomId : room,
		video: true,
		audio: true
	}
	sendMessage(message);
}

function onNewParticipant(request) {
	receiveVideo(request.member); // TODO : ??
}

function receiveVideoResponse(result) {
	participants[result.userId].rtcPeer.processAnswer (result.sdpAnswer, function (error) {
		if (error){
			alert(error);
			return console.error (error);}
	});
}

function timerResponse(request){
	// 만약 서버로부터 request를 받으면
	console.log(request.userId + ' timer : ' + request.timerState+' comeon!');
	var participant = participants[request.userId];
	// 해당 유저의 participant 객체의 timerState를 변경시키고 그에 따른 시간을 출력해준다.
	/*


	 */

}

function callResponse(message) {
	if (message.response != 'accepted') {
		console.info('Call not accepted by peer. Closing call');
		stop();
	} else {
		webRtcPeer.processAnswer(message.sdpAnswer, function (error) {
			if (error) {
				alert(error);
				return console.error (error);}
		});
	}
}

function onExistingParticipants(msg) {
	var constraints = {
		audio : true,
		video : {
			mandatory : {
				maxWidth : 320,
				maxFrameRate : 30,
				minFrameRate : 30
			}
		}
	};
	console.log(name + " registered in room " + room);
	var participant = new Participant(name);
	participants[name] = participant;
	var video = participant.getVideoElement();

	var options = {
	      localVideo: video,
	      mediaConstraints: constraints,
	      onicecandidate: participant.onIceCandidate.bind(participant)
	    }
	participant.rtcPeer = new kurentoUtils.WebRtcPeer.WebRtcPeerSendonly(options,
		function (error) {
		  if(error) {
			  alert(error);
			  return console.error(error);
		  }
		  this.generateOffer (participant.offerToReceiveVideo.bind(participant));
	});

	msg.members.forEach(receiveVideo);
}

function timerStart(){
	sendMessage({
		id : 'timerState',
		timerState : true,
		time : getCurrentTime()
	});
}

function timerStop(){
	sendMessage({
		id : 'timerState',
		timerState : false,
		time : getCurrentTime()
	});


}

function getCurrentTime() {
	const now = new Date();
	let hours = now.getHours();
	let minutes = now.getMinutes();
	let seconds = now.getSeconds();

	// 시간, 분, 초가 한 자리 숫자일 경우 앞에 0을 추가합니다.
	hours = hours < 10 ? '0' + hours : hours;
	minutes = minutes < 10 ? '0' + minutes : minutes;
	seconds = seconds < 10 ? '0' + seconds : seconds;

	const currentTime = hours + ':' + minutes + ':' + seconds;
	return currentTime;
}

function leaveRoom() {
	// sendMessage({
	// 	id : 'leaveRoom'
	// });

	for ( var key in participants) {
		participants[key].dispose();
	}

	document.getElementById('join').style.display = 'block';
	document.getElementById('room').style.display = 'none';

	ws.close();
}

function receiveVideo(sender) {
	let userId = sender.userId;
	let videoState = sender.video;
	let audioState = sender.audio;
	let timerState = sender.timer;
	let studyTime = sender.studyTime;
	let onTime = sender.onTime;

	var participant = new Participant(userId, videoState, audioState, timerState, studyTime, onTime);
	participants[userId] = participant;
	var video = participant.getVideoElement();

	var options = {
		connectionConstraints: {
			offerToReceiveAudio: true,
			offerToReceiveVideo: true,
		},
      remoteVideo: video,
      onicecandidate: participant.onIceCandidate.bind(participant)
    }

	participant.rtcPeer = new kurentoUtils.WebRtcPeer.WebRtcPeerRecvonly(options,
			function (error) {
			  if(error) {
				  alert(error);
				  return console.error(error);
			  }
			  this.generateOffer (participant.offerToReceiveVideo.bind(participant));
	});;
}

function onParticipantLeft(request) {
	console.log('Participant ' + request.userId + ' left');
	var participant = participants[request.userId];
	participant.dispose();
	delete participants[request.userId];
}

function sendMessage(message) {
	var jsonMessage = JSON.stringify(message);
	console.log('Sending message: ' + jsonMessage);
	ws.send(jsonMessage);
}
