const PARTICIPANT_MAIN_CLASS = 'participant main';
const PARTICIPANT_CLASS = 'participant';

function Participant(userId, videoState, audioState, timerState, studyTime, onTime) {
	console.log("참여자명 : "+userId)

	this.userId = userId;
	this.videoState = videoState;
	this.audioState = audioState;
	this.timerState = timerState;
	this.studyTime = studyTime;
	this.onTime = onTime;

	var container = document.createElement('div');
	container.className = isPresentMainParticipant() ? PARTICIPANT_CLASS : PARTICIPANT_MAIN_CLASS;
	container.id = userId;
	container.onclick = switchContainerClass;


	var video = document.createElement('video');
	container.appendChild(video);
	video.id = 'video-' + userId;
	video.autoplay = true;
	video.playsInline = true;
	video.controls = false;


	var span = document.createElement('span');
	span.appendChild(document.createTextNode(userId));
	span.appendChild(document.createTextNode("/"));
	span.appendChild(document.createTextNode(studyTime));
	span.appendChild(document.createTextNode("/"));
	span.appendChild(document.createTextNode(videoState));
	span.appendChild(document.createTextNode("/"));
	span.appendChild(document.createTextNode(audioState));
	span.appendChild(document.createTextNode("/"));
	span.appendChild(document.createTextNode(timerState));
	container.appendChild(span);

	var rtcPeer;

	document.getElementById('participants').appendChild(container);


	this.getElement = function() {
		return container;
	}

	this.getVideoElement = function() {
		return video;
	}

	function switchContainerClass() {
		if (container.className === PARTICIPANT_CLASS) {
			var elements = Array.prototype.slice.call(document.getElementsByClassName(PARTICIPANT_MAIN_CLASS));
			elements.forEach(function(item) {
				item.className = PARTICIPANT_CLASS;
			});

			container.className = PARTICIPANT_MAIN_CLASS;
		} else {
			container.className = PARTICIPANT_CLASS;
		}
	}

	function isPresentMainParticipant() {
		return ((document.getElementsByClassName(PARTICIPANT_MAIN_CLASS)).length != 0);
	}

	this.offerToReceiveVideo = function(error, offerSdp, wp){
		if (error) return console.error ("sdp offer error")
		console.log('Invoking SDP offer callback function');
		var msg =  { id : "receiveVideoFrom",
			userId : userId,
			sdpOffer : offerSdp
		};
		sendMessage(msg);
	}


	this.onIceCandidate = function (candidate, wp) {
		console.log("Local candidate" + JSON.stringify(candidate));

		var message = {
			id: 'onIceCandidate',
			userId : userId,
			candidate: candidate
		};
		sendMessage(message);
	}

	Object.defineProperty(this, 'rtcPeer', { writable: true});

	this.dispose = function() {
		console.log('Disposing participant ' + this.userId);
		this.rtcPeer.dispose();
		container.parentNode.removeChild(container);
	};

}

