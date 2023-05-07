/*
 * (C) Copyright 2014 Kurento (http://kurento.org/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

var ws = new WebSocket('wss://localhost:8443/socket');
// var ws = new SockJS('wss://localhost:8443/socket', null, {
// 	transports: ["websocket", "xhr-streaming", "xhr-polling"]
// });

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
		console.info("!!!!!!!!!why existingParticipants");
		onExistingParticipants(parsedMessage);
		break;
	case 'newParticipantArrived':
		console.info("!!!!!!!!!why new");
		onNewParticipant(parsedMessage);
		break;
	case 'participantLeft':
		console.info("!!!!!!!!!why left");
		onParticipantLeft(parsedMessage);
		break;
	case 'receiveVideoAnswer':
		console.info("!!!!!!!!!why receive~");
		receiveVideoResponse(parsedMessage);
		break;
	case 'onIceCandidate':
		console.info("!!!!!!!!!why addIceCandidate 전");
		participants[parsedMessage.userId].rtcPeer.addIceCandidate(parsedMessage.candidate, function (error) {
	        console.info("!!!!!!!!!why addIceCandidate 후");
			if (error) {
		      console.error("Error adding candidate: " + error);
			  alert(error);
		      return;
	        }
	    });
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

function leaveRoom() {
	sendMessage({
		id : 'leaveRoom'
	});

	for ( var key in participants) {
		participants[key].dispose();
	}

	document.getElementById('join').style.display = 'block';
	document.getElementById('room').style.display = 'none';

	ws.close();
}

function receiveVideo(sender) {
	let userId = sender.userId;
	let videoStatus = sender.video;
	let audioStatus = sender.audio;

	var participant = new Participant(userId, videoStatus, audioStatus);
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
