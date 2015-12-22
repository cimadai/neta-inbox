/// <reference path="./third_party/jquery/jquery.d.ts" />
/// <reference path="./third_party/jquery/jquery.cookie.d.ts" />
/// <reference path="./third_party/bootstrap/bootstrap-notify.d.ts" />

namespace Alice.Notify {
	var notifySettings = {
		// settings
		element: 'body',
		position: null,
		type: "info",
		allow_dismiss: true,
		newest_on_top: false,
		showProgressbar: false,
		placement: {
			from: "top",
			align: "right"
		},
		offset: 60,
		spacing: 10,
		z_index: 1031,
		delay: 5000,
		timer: 1000,
		url_target: '_blank',
		mouse_over: null,
		animate: {
			enter: "animated fadeInDown",
			exit: "animated fadeOutUp"
		},
		onShow: null,
		onShown: null,
		onClose: null,
		onClosed: null,
		icon_type: 'class',
		template: '<div data-notify="container" class="col-xs-11 col-sm-3 alert alert-{0}" role="alert">' +
		'<button type="button" aria-hidden="true" class="close" data-notify="dismiss">×</button>' +
		'<span data-notify="icon"></span> ' +
		'<span data-notify="title">{1}</span> ' +
		'<span data-notify="message">{2}</span>' +
		'<div class="progress" data-notify="progressbar">' +
		'<div class="progress-bar progress-bar-{0}" role="progressbar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100" style="width: 0%;"></div>' +
		'</div>' +
		'<a href="{3}" target="{4}" data-notify="url"></a>' +
		'</div>'
	};

	function showAllFromDOM () {
		$(".notify").each(function () {
			var $self = $(this);
			var type = $self.is(".danger") ? "danger" : "success";
			var settings = $.extend({}, notifySettings, {type: type});
			$.notify({
				// options
				// icon: 'glyphicon glyphicon-warning-sign',
				message: $self.text()
			}, settings);
		});
	}

	export function error(text) {
		var settings = $.extend({}, notifySettings, {type: "danger"});
		$.notify({message: text}, settings);
	}
	export function success(text) {
		var settings = $.extend({}, notifySettings, {type: "success"});
		$.notify({message: text}, settings);
	}
	export function pushSuccessToCookie(text) {
		var notifies = this.getNotifiesFromCookie();
		notifies.push({type: "success", message: text});
		$.cookie('notifies', JSON.stringify(notifies), {path: "/"});
	}
	export function pushErrorToCookie(text) {
		var notifies = this.getNotifiesFromCookie();
		notifies.push({type: "danger", message: text});
		$.cookie('notifies', JSON.stringify(notifies), {path: "/"});
	}
	export function getNotifiesFromCookie() {
		var notifies = [];
		// cookieからnotify用の配列を取得
		var json = $.cookie('notifies');
		try {
			// 配列じゃなかったら無視
			var target = JSON.parse(json);
			if ($.isArray(target)) {
				notifies = target;
			}
		} catch (e) {
		}

		// 取得したら削除
		$.cookie('notifies', null, {path: "/"});
		return notifies;
	}

	var notifies = getNotifiesFromCookie();
	if ($.isArray(notifies)) {
		$.each(notifies, function (idx, val) {
			if (this.type === "success") {
				this.success(this.message);
			} else {
				this.error(this.message);
			}
		});
	}

	// DOMにある通知をすべて表示する。
	showAllFromDOM();
}
