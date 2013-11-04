$.fn.isVisible = function() {
	return $.expr.filters.visible(this[0]);
};

var currentPage;
var selectedMails = [];

function showInbox() {
	$.getJSON("/msg/ajax/inbox", function(data) {
		currentPage = "inbox";
		selectedMails = []
		updateBottomBar();

		$("#inboxTabBtn").addClass("selected");
		$("#outboxTabBtn").removeClass("selected");
		$("#mailList").empty();

		data.payload.forEach(function(mail) {
			var template = $("#inboxItemTmpl").html();
			var mailObj = {
				"mail" : mail[0],
				"sender" : mail[1]
			};
			var mailDiv = $("<div/>").html(Mustache.to_html(template, mailObj)).contents();
			mailDiv.children(".mail-title-bar").click(function() {
				showMail(mailObj, mailDiv);
			});
			mailDiv.find(".mail-controls input[type='checkbox']").change(function() {
				var index = selectedMails.indexOf(mailObj.mail.id);
				if ($(this).is(":checked")) {
					(index == -1) && selectedMails.push(mailObj.mail.id);
				} else {
					(index != -1) && selectedMails.splice(index, 1);
				}
				updateBottomBar();
			});
			$("#mailList").append(mailDiv);
		});

		window.parent.updateMessageCounts(data.unreadMail, data.unreadNotification);
	});
}

function showOutbox() {
	$.getJSON("/msg/ajax/outbox", function(data) {
		currentPage = "outbox";
		selectedMails = []
		updateBottomBar();

		$("#inboxTabBtn").removeClass("selected");
		$("#outboxTabBtn").addClass("selected");
		$("#mailList").empty();

		data.payload.forEach(function(mail) {
			var template = $("#outboxItemTmpl").html();
			var mailObj = {
				"mail" : mail[0],
				"receiver" : mail[1]
			};
			var mailDiv = $("<div/>").html(Mustache.to_html(template, mailObj)).contents();
			mailDiv.children(".mail-title-bar").click(function(event) {
				showMail(mailObj, mailDiv);
			});
			$("#mailList").append(mailDiv);
		});

		window.parent.updateMessageCounts(data.unreadMail, data.unreadNotification);
	});
}

function showMail(mailObj, mailDiv) {
	if (!$(mailDiv).children(".mail-content-inbox, .mail-content-outbox").isVisible()) {
		if (mailObj.mail.isRead == false) {
			$(mailDiv).find(".mail-title-bar").addClass("mail-read");
			$.post("/msg/markRead", {
				"selectedMails" : [ mailObj.mail.id ]
			}, function(data) {
				// piggyback
				window.parent.updateMessageCounts(data.unreadMail, data.unreadNotification);
			});
		}
		$(mailDiv).children(".mail-content-inbox, .mail-content-outbox").html(mailObj.mail.content).show(100);
	} else {
		$(mailDiv).children(".mail-content-inbox, .mail-content-outbox").html(mailObj.mail.content).hide(100);
	}
}

function selectAll() {
	$("#mailList .mail-controls input[type='checkbox']").each(function() {
		if (!$(this).is(":checked")) {
			$(this).prop("checked", true).trigger("change");
		}
	});
}

function trash() {
	$.post("/msg/trashMail", {
		"selectedMails" : selectedMails
	}, function(data) {
		// piggyback
		window.parent.updateMessageCounts(data.unreadMail, data.unreadNotification);
		showInbox();
	});
}

function markRead() {
	$.post("/msg/markRead", {
		"selectedMails" : selectedMails
	}, function(data) {
		// piggyback
		window.parent.updateMessageCounts(data.unreadMail, data.unreadNotification);
		showInbox();
	});
}

function updateBottomBar() {
	currentPage == "inbox" ? $("#bottomBarControls").show() : $("#bottomBarControls").hide();
	$("#trashBtn, #markReadBtn").prop("disabled", selectedMails.length == 0);
}

function showNotifications() {
	$.getJSON("/msg/ajax/notifications", function(data) {
		data.payload.forEach(function(notification) {
			var template = $("#notificationTmpl").html();
			var notificationDiv = $("<div/>").html(Mustache.to_html(template, notification)).contents();
			$("#notificationList").append(notificationDiv);
		});

		window.parent.updateMessageCounts(data.unreadMail, data.unreadNotification);
	});
}