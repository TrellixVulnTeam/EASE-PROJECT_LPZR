var constructorForm = function(rootEl, parent) {
	var self = this;
	this.qRoot = rootEl;
	this.oParent = parent;
	this.oInputs = [];
	this.params = {};
	this.qRoot.find('input').each(function(index, elem) {
		this.oErrorMsg;

		var oClass = $(elem).attr('oClass');
		if (oClass != null) {
			self.oInputs.push(new Input[oClass]($(elem), self));
		}
	});
	this.qRoot.find('div').each(function(index, elem) {
		var oClass = $(elem).attr('oClass');
		if (oClass != null) {
			self.oErrorMsg = new ErrorMsg[oClass]($(elem), self);
		}
	});
	this.qButton = this.qRoot.find("button[type='submit']");
	this.qButton.click(function(e) {
		self.submit(e);
	});
	this.isEnabled = false;
	for (var i = 0; i < this.oInputs.length; ++i) {
		this.oInputs[i].listenBy(this.qRoot);
	}
	self.isEnabled = false;
	self.qButton.prop('disabled', true);
	self.qButton.removeClass("Active");

	this.enable = function() {
		self.isEnabled = true;
		self.qButton.prop('disabled', false);
		self.qButton.addClass("Active");
	};
	this.disable = function() {
		self.isEnabled = false;
		self.qButton.prop('disabled', true);
		self.qButton.removeClass("Active");
	};
	this.qRoot.on("StateChanged", function() {
		if (self.checkInputs())
			self.enable();
		else
			self.disable();
	});
	this.checkInputs = function() {
		for (var i = 0; i < self.oInputs.length; ++i) {
			if (self.oInputs[i].isValid == false) {
				return false;
			}
		}
		return true;
	};
	this.findByName = function(aString) {
		var res = null;
		self.oInputs.forEach(function(element) {
			if (element.qInput.attr("name") == aString) {
				res = element.qInput;
				return;
			}
		});
		return res;
	}
	this.reset = function() {
		self.disable();
		for (var i = 0; i < self.oInputs.length; ++i) {
			self.oInputs[i].reset();
		}
		self.afterReset();
	};
	this.afterReset = function() {
		
	};
	this.submit = function(e) {
		e.preventDefault();
		self.oInputs.forEach(function(elem) {
			self.params[elem.qInput.attr('name')] = elem.getVal();
		});
		self.beforeSubmit();
		postHandler.post(self.qRoot.attr('action'), self.params,
				self.afterSubmit, self.successCallback, self.errorCallback);
	};
	this.beforeSubmit = function() {
		
	};
	this.afterSubmit = function() {
		
	};
	this.successCallback = function() {

	};
	this.errorCallback = function() {

	}
}

var Form = {
	EditUserNameForm : function(rootEl) {
		constructorForm.apply(this, arguments);
		var self = this;
		this.successCallback = function() {
			showAlertPopup('Modifications successfully applied !', false);
			$("#userSettingsButton span").html(self.oInputs[0].getVal());
			self.disable();
		};
		this.errorCallback = function(retMsg) {
			$(".errorMessage", self.qRoot).addClass("show");
		}
	},
	EditUserPasswordForm : function(rootEl) {
		constructorForm.apply(this, arguments);
		var self = this;
		this.successCallback = function (retMsg) {
			$("p.response", self.qRoot).removeClass("error");
			$("p.response", self.qRoot).addClass("success");
			$("p.response", self.qRoot).text(retMsg);
		}
		this.errorCallback = function (retMsg) {
			$("p.response", self.qRoot).removeClass("success");
			$("p.response", self.qRoot).addClass("error");
			$("p.response", self.qRoot).text(retMsg);
		}
		this.afterSubmit = function () {
			self.reset();
		}
	},
	AddAppForm : function(rooEl) {
		constructorForm.apply(this, arguments);
		var self = this;
		this.newAppItem = null;
		this.appsContainer = null;
		this.helper = null;
		this.attributesToSet = {};
		this.site_id = null;
		this.profile_id = null;
		this.app_id = null;
		this.postName = 'addApp';
		this.helper = null;
		this.setHelper = function(jqHelper) {
			self.helper = jqHelper;
			self.site_id = self.helper.attr("idx");
			var attrs = jqHelper.attr();
			for (var key in attrs) {
				if (!attrs.hasOwnProperty(key))
					continue;
				switch (key) {
					case "team":
						var newInput = $(".classicLogin", self.qRoot).append("<input type='" + attrs[key] + "' name='" + key + "' oClass='NoEmptyInput' placeholder='Team name' />");
						var newInputObj = new Input["NoEmptyInput"]($("input[name='" + key + "']", self.qRoot), self);
						newInputObj.listenBy(self.qRoot);
						self.oInputs.push(newInputObj);
						break;
					default:
						break;
				}
				
			}
			self.checkInputs();
		}
		this.setAppsContainer = function(qObject) {
			self.appsContainer = qObject;
			self.profile_id = self.appsContainer.closest('.item').attr('id');
		}
		this.setNewAppItem = function(qObject) {
			self.newAppItem = qObject;
		}
		this.removeAddedFields = function() {
			self.oInputs.forEach(function(element) {
				if (element.qInput.attr("name") != "login" && element.qInput.attr("name") != "password" && element.qInput.attr("name") != "name") {
					element.qInput.remove();
					var index = self.oInputs.indexOf(element);
					self.oInputs.splice(index, 1);
				}
			});
		};
		this.afterReset = function() {
			self.removeAddedFields();
		};
		this.beforeSubmit = function() {
			self.oParent.close();
			self.appsContainer.append(self.newAppItem);
			self.params = {
				profileId : self.profile_id,
				siteId : self.site_id,
				appId : self.app_id
			};
			self.oInputs.forEach(function(element) {
				var elemName = element.qInput.attr("name");
				var elemValue = element.qInput.val();
				self.params[elemName] = elemValue;
				if (elemName != "password") {
					self.attributesToSet[elemName] = elemValue;
				}
			});
		}
		this.afterSubmit = function() {
			self.removeAddedFields();
		}
		this.successCallback = function(retMsg) {
			var x = parseInt($(".catalogApp[idx='" + self.site_id + "'] span.apps-integrated i.count").html());
			$(".catalogApp[idx='" + self.site_id + "'] span.apps-integrated i.count").html(x+1);
			$(".catalogApp[idx='" + self.site_id + "'] span.apps-integrated").addClass("showCounter");
			self.newAppItem.find('.linkImage').addClass('scaleOutAnimation');
			setTimeout(function() {
				self.newAppItem.find('.linkImage').removeClass(
						'scaleOutAnimation');
				self.newAppItem = null;
			}, 1000);
			self.newAppItem.find('.linkImage').attr('onclick',
					"sendEvent(this)");
			easeTracker.trackEvent('App added');
			easeTracker.trackEvent($(".catalogApp[idx='" + self.site_id + "']").attr('name') + " app added");
			self.newAppItem.attr('webId', self.helper.attr('idx'));
			self.newAppItem.attr('logwith', (self.app_id == null) ? 'false'
					: self.app_id);
			self.newAppItem.find('.siteName p').text(self.oInputs[0].getVal());
			self.newAppItem.attr('id', retMsg);
			self.newAppItem.attr('ssoid', self.helper.attr('data-sso'));
			for (var key in self.attributesToSet) {
				if (!self.attributesToSet.hasOwnProperty(key))
					continue;
				self.newAppItem.attr(key, self.attributesToSet[key]);
			}
			var emailRegex = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
			if (emailRegex.test(self.oInputs[1].getVal()) && !$(".email-suggestion[email='" + self.oInputs[1].getVal() + "']").length) {
				$(".suggested-emails").append(
						"<p class='email-suggestion' email='" + self.oInputs[1].getVal() + "'><span>"
								+ self.oInputs[1].getVal() + "</span></p>");
				$("#editVerifiedEmails").append("<div class='emailLine'>"
						+ "<input type='email' name='email' oClass='HiddenInput' value='" + self.oInputs[1].getVal() + "'readonly />"
						+ " <span class='unverifiedEmail'><span class='verify'>Verified ?</span><span class='sendVerificationEmail'>Send verification email</span></span>"
						+ "<div class='sk-fading-circle email-loading'>"
						+ "	<span>We are sending you an email</span>"
						+ "	<div class='sk-circle1 sk-circle'></div>"
						+ '	<div class="sk-circle2 sk-circle"></div>'
						+ '	<div class="sk-circle3 sk-circle"></div>'
						+ '	<div class="sk-circle4 sk-circle"></div>'
						+ '	<div class="sk-circle5 sk-circle"></div>'
						+ '	<div class="sk-circle6 sk-circle"></div>'
						+ '	<div class="sk-circle7 sk-circle"></div>'
						+ '	<div class="sk-circle8 sk-circle"></div>'
						+ '	<div class="sk-circle9 sk-circle"></div>'
						+ ' <div class="sk-circle10 sk-circle"></div>'
						+ '	<div class="sk-circle11 sk-circle"></div>'
						+ '	<div class="sk-circle12 sk-circle"></div>'
						+ '</div>'
					+ "</div>");
			}
			self.reset();
			self.appsContainer = null;
			self.helper = null;
			self.attributesToSet = {};
			self.site_id = null;
			self.profile_id = null;
			self.app_id = null;
			self.setPostName('addApp');
			self.helper = null;
		}
		this.errorCallback = function(retMsg) {
			easeTracker.trackEvent("Add app failed");
			easeTracker.trackEvent($(".catalogApp[idx='" + self.site_id + "']").attr('name') + " add app failed");
			self.newAppItem.remove();
			self.reset();
			$(parent).find('.alertDiv').addClass('show');
			showAlertPopup(retMsg, true);
		}
		this.setPostName = function(postName) {
			self.postName = postName;
			self.qRoot.attr("action", self.postName);
			self.qRoot.trigger("StateChanged");
		}
		this.checkInputs = function() {
			if (self.postName != 'addApp')
				return true;
			for (var i = 0; i < self.oInputs.length; ++i) {
				if (self.oInputs[i].isValid == false) {
					return false;
				}
			}
			return true;
		}
		self.oInputs[1].onEnter(function(e) {
			e.preventDefault();
			self.oInputs[2].focus();
		});
	},
	DeleteProfileForm : function(rootEl) {
		constructorForm.apply(this, arguments);
		var self = this;
		this.beforeSubmit = function() {
			$('#loading').addClass('la-animate');
		}
		this.afterSubmit = function() {
			$('#loading').removeClass("la-animate");
			self.qRoot.find('.AccountApp.selected').removeClass("selected");
		}
		this.successCallback = function(retMsg) {
			self.oParent.close();
			self.oParent.targetProfile.remove();
//			window.location.replace("index.jsp");
		};
		this.errorCallback = function(retMsg) {
			$(parent).find('.alertDiv').addClass('show');
			$(parent).find('#password').val('');
			showAlertPopup(retMsg, true);
		}
	},
	ModifyAppForm : function(rootEl) {
		constructorForm.apply(this, arguments);
		var self = this;
		this.oPopup = null;
		this.app = null;
		this.password = null;
		this.appId = null;
		this.aId = null;
		this.attributesToSet = {};
		this.setApp = function(jObject) {
			self.app = jObject;
			self.appId = self.app.attr("id");
			self.aId = self.app.attr("aid");
			var objAttr = jObject.attr();
			for (var key in objAttr) {
				if (!objAttr.hasOwnProperty(key))
					continue;
				switch (key) {
					case "login":
						self.oInputs[1].val(objAttr[key]);
						break;
					case "name":
						self.oInputs[0].val(objAttr[key]);
						break;
					case "team":
						var existingInput = $(".classicLogin input[name='" + key + "']", self.qRoot);
						if (!existingInput.length) {
							var newInput = $(".classicLogin", self.qRoot).append("<input type='" + objAttr[key] + "' name='" + key + "' oClass='NoEmptyInput' placeholder='Team name' />");
							var newInputObj = new Input["NoEmptyInput"]($("input[name='" + key + "']", self.qRoot), self);
							newInputObj.listenBy(self.qRoot);
							self.oInputs.push(newInputObj);
							newInputObj.val(objAttr[key]);
						} else {
							existingInput.val(objAttr[key]);
							existingInput.change();
						}
						break;
					default:
						break;
				}
			}
		}
		this.removeAddedFields = function() {
			self.oInputs.forEach(function(element) {
				if (element.qInput.attr("name") != "login" && element.qInput.attr("name") != "password" && element.qInput.attr("name") != "name") {
					element.qInput.remove();
					var index = self.oInputs.indexOf(element);
					self.oInputs.splice(index, 1);
				}
			});
		};
		this.afterReset = function() {
			self.removeAddedFields();
		};
		this.submit = function(e) {
			e.preventDefault();
			var AppToLoginWith = self.qRoot.find('.AccountApp.selected');
			if (AppToLoginWith.length) {
				self.aId = AppToLoginWith.attr('aid');
			}
			self.oInputs.forEach(function(element) {
				var elemName = element.qInput.attr("name");
				var elemValue = element.qInput.val();
				self.params[elemName] = elemValue;
				if (elemName != "password")
					self.attributesToSet[elemName] = elemValue;
				else {
					if (elemValue != null && elemValue != "" ) {
						self.password = elemValue;
					}
						
				}
			});
			self.params = {
				name : self.oInputs[0].getVal(),
				appId : self.appId,
				lwId : self.aId	
			};
			if (self.password != null)
				self.params["wPassword"] = self.password;
			for (var key in self.attributesToSet) {
				if (!self.attributesToSet.hasOwnProperty(key))
					continue;
				self.params[key] = self.attributesToSet[key];
			}
			$
					.post(
							'editApp',
							self.params,
							function(data) {
								var retMsg = data.substring(4);
								var image = self.app.find('.linkImage');
								self.oParent.close();
								image.addClass('scaleOutAnimation');
								setTimeout(function() {
									image.removeClass('scaleOutAnimation');
								}, 1000);
								for (var key in self.attributesToSet) {
									if (!self.attributesToSet.hasOwnProperty(key))
										continue;
									self.app.attr(key, self.attributesToSet[key]);
								}
								self.app.attr('logwith', (self.oInputs[1].getVal().length || self.aId) == null ? 'false' : self.aId);
								self.app.find('.siteName p').text(self.oInputs[0].getVal());
								self.app.find('.emptyAppIndicator').remove();
								self.app.removeClass('emptyApp');
								self.qRoot.find('.AccountApp.selected').removeClass("selected");
								self.removeAddedFields();
							});
		}
	},
	DeleteAppForm : function(rootEl) {
		constructorForm.apply(this, arguments);
		var self = this;
		this.beforeSubmit = function() {
			$(self.oParent.app).find('.linkImage').addClass('easyScaling');
		}
		this.afterSubmit = function() {
			self.oParent.close();
			$(self.oParent.app).find('.linkImage').removeClass('easyScaling');
		}
		this.successCallback = function(retMsg) {
			var json = JSON.parse(retMsg);
			var webId = (self.oParent.app).attr("webid");
			var x = parseInt($(".catalogApp[idx='" + webId + "'] span.apps-integrated i.count").html());
			$(".catalogApp[idx='" + webId + "'] span.apps-integrated i.count").html(x-1);
			if (x == 1)
				$(".catalogApp[idx='" + webId + "'] span.apps-integrated").removeClass("showCounter");
			$(self.oParent.app).find('.linkImage').addClass('deletingApp');
			$(".suggested-emails p[email='" + json['email'] + "']").remove();
			$("#editVerifiedEmails .emailLine input[value='" + json['email'] + "']").parent().remove();
			setTimeout(function() {
				self.oParent.app.remove();
			}, 500);
		};
		this.errorCallback = function(retMsg) {
			showAlertPopup(retMsg, true);
		}
	},
	AddEmailForm : function(rootEl) {
		constructorForm.apply(this, arguments);
		var self = this;
		this.addEmail = function(emailVal) {
			$("#editVerifiedEmails").append("<div class='emailLine'>"
			+ "<input type='email' name='email' oClass='HiddenInput' value='" + emailVal + "'readonly />"
			+ " <span class='unverifiedEmail'><span class='verify'>Verified ?</span><span class='sendVerificationEmail'>Send verification email</span></span>"
			+ "<div class='sk-fading-circle email-loading'>"
			+ "	<span>We are sending you an email</span>"
			+ "	<div class='sk-circle1 sk-circle'></div>"
			+ '	<div class="sk-circle2 sk-circle"></div>'
			+ '	<div class="sk-circle3 sk-circle"></div>'
			+ '	<div class="sk-circle4 sk-circle"></div>'
			+ '	<div class="sk-circle5 sk-circle"></div>'
			+ '	<div class="sk-circle6 sk-circle"></div>'
			+ '	<div class="sk-circle7 sk-circle"></div>'
			+ '	<div class="sk-circle8 sk-circle"></div>'
			+ '	<div class="sk-circle9 sk-circle"></div>'
			+ ' <div class="sk-circle10 sk-circle"></div>'
			+ '	<div class="sk-circle11 sk-circle"></div>'
			+ '	<div class="sk-circle12 sk-circle"></div>'
			+ '</div>'
		+ "</div>");
			$(".suggested-emails").append(
					"<p class='email-suggestion' email='" + emailVal
							+ "'><span>" + emailVal + "</span></p>");
		};
		this.beforeSubmit = function() {
			$("#AddEmailPopup").addClass("md-show");
			$("#AddEmailPopup .waiting").addClass("show");
		}
		this.successCallback = function(retMsg) {
			$("#AddEmailPopup .waiting").removeClass("show");
			$("#AddEmailPopup .email-sent").addClass("show");
			setTimeout(function() {
				$("#AddEmailPopup").removeClass("md-show");
				$("#AddEmailPopup .waiting, #AddEmailPopup .email-sent").removeClass("show");
			}, 2000);
			self.addEmail(self.oInputs[0].getVal());
			$(".newEmail").addClass("show");
			$(".newEmailInput").removeClass("show");
			self.reset();
		}
	},
	DeleteEmailForm : function(rootEl) {
		constructorForm.apply(this, arguments);
		var self = this;
		this.successCallback = function(retMsg) {
			$(".emailLine input[value='" + self.oInputs[0].getVal() + "']").parent().remove();
			$(".email-suggestion[email='" + self.oInputs[0].getVal() + "']").remove();
			var x = $(".verifiedEmail").length;
			if (x > 1)
				$(".integrated-emails-count span").html(x + " validated emails");
			else
				$(".integrated-emails-count span").html(x + " validated email");
			self.reset();
			self.oParent.close();
		};
	},
	SendVerificationEmailForm : function(rootEl) {
		constructorForm.apply(this, arguments);
		var self = this;
		this.setEmail = function(email) {
			self.oInputs[0].val(email);
		};
		this.beforeSubmit = function() {
			$(".emailLine").has("input[value='" + self.oInputs[0].getVal() + "']").find(".unverifiedEmail").addClass("wait");
			$(".emailLine").has("input[value='" + self.oInputs[0].getVal() + "']").find(".email-loading").addClass("show");
		};
		this.afterSubmit = function() {
			setTimeout(function() {
				$(".emailLine").has("input[value='" + self.oInputs[0].getVal() + "']").find(".email-loading").removeClass("show");
				$(".emailLine").has("input[value='" + self.oInputs[0].getVal() + "']").find(".email-sent").addClass("show");
			}, 2000);
		}
	},
	DeleteAccountForm : function(rootEl) {
		constructorForm.apply(this, arguments);
		var self = this;
		this.beforeSubmit = function() {
			self.qRoot.removeClass("show");
			$("#DeleteAccountWait").addClass("md-show");
			$(".wait", self.oParent.qRoot).addClass("show");
		};
		this.successCallback = function(retMsg) {
			setTimeout(function() {
				window.location = "index.jsp";
			}, 1000);
		};
		this.errorCallback = function(retMsg) {
			$(".wait", self.oParent.qRoot).removeClass("show");
			self.qRoot.addClass("show");
			$(".errorMessage", self.oParent.qRoot).addClass("show");
		};
	},
	AddUpdateForm : function(rootEl) {
		constructorForm.apply(this, arguments);
		var self = this;
		this.beforeSubmit = function() {
		};
		this.successCallback = function(retMsg) {
			self.oParent.close();
			profiles.forEach(function (item) {
				if (item.id == self.params['profileId']) {
					item.addApp(self.params['login'], self.params['siteId'], self.params['name'], retMsg, "", false, self.qRoot.find("img").attr("src"));
					console.log(self.oParent.updateIndex);
					catalog.oUpdate.updates[self.oParent.updateIndex].remove();
				}
			});
			
		};
		this.errorCallback = function(retMsg) {
			self.oParent.error.html(retMsg);
		};
	},
	RegisterForm : function(rootEl) {
		constructorForm.apply(this, arguments);
		var self = this;
		this.email = $("input[name='email']", self.qRoot).val();
		this.invitationCode = $("input[name='invitationCode']", self.qRoot).val();
		this.params = {
				email : self.email,
				invitationCode : self.invitationCode
		};
		this.beforeSubmit = function() {
			$(".successHelper", self.qRoot).removeClass("success");
			$(".errorHelper", self.qRoot).removeClass("error");
			$(".loadHelper, button[type='submit']", self.qRoot).addClass("loading");
		};
		this.successCallback = function(retMsg) {
			$(".loadHelper", self.qRoot).removeClass("loading");
			$(".successHelper p", self.qRoot).text(retMsg);
			$(".successHelper", self.qRoot).addClass("success");
			setTimeout(function() {
				window.location= "index.jsp";
			}, 750);
		};
		this.errorCallback = function(retMsg) {
			$(".loadHelper, button[type='submit']", self.qRoot).removeClass("loading");
			$(".errorHelper p", self.qRoot).text(retMsg);
			$(".errorHelper", self.qRoot).addClass("error");
		}
	},
	GetEmailForm : function(rootEl) {
		constructorForm.apply(this, arguments);
		var self = this;
		this.beforeSubmit = function () {
			$(".loadHelper", self.qRoot).addClass("loading");
			$(".errorHelper", self.qRoot).removeClass("error");
			$(".successHelper", self.qRoot).removeClass("success");
			self.qRoot.addClass("loading");
		};
		this.successCallback = function(retMsg) {
			$(".loadHelper", self.qRoot).removeClass("loading");
			self.qRoot.removeClass("loading");
			$(".successHelper p", self.qRoot).text(retMsg);
			$(".successHelper", self.qRoot).addClass("success");
			self.reset();
		};
		this.errorCallback = function(retMsg) {
			$(".loadHelper", self.qRoot).removeClass("loading");
			self.qRoot.removeClass("loading");
			$(".errorHelper p", self.qRoot).text(retMsg);
			$(".errorHelper", self.qRoot).addClass("error");
			self.reset();
		};
	}
}