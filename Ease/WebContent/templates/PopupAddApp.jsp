<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<script src="js/addApp.js"></script>
<div class="md-modal md-effect-15 popup" id="PopupAddApp">
	<div class="md-content">
		<div class="popupHeader">
			<img class="logoApp" src="" />
			<div class="textContent">
				<p>Type your password for the last time ;)</p>
			</div>
		</div>
		<div class="lineInput">
			<p class="inputTitle">App name :</p>
			<input  id="name" name="name" type="text" placeholder="Name" maxlength="14"/>
		</div>
		<div class="loginWithChooser">
			<div class="linedSeparator">
				<div class="backgroundLine"></div>
				<p>Log in with</p>
			</div>
			<div class="loginWithButton facebook hidden" webid="7"><p>Facebook</p></div>
			<div class="loginWithButton linkedin hidden" webid="28"><p>Linkedin</p></div>
			<div class="linedSeparator or">
				<div class="backgroundLine"></div>
				<p>or</p>
			</div>
		</div>
		<div class="loginAppChooser" style="display:none;">
			<p>Select your account </p>
			<div class="buttonBack">
				<i class="fa fa-arrow-circle-o-left" aria-hidden="true"></i>
			</div>
			<div class="ChooserContent">
			</div>
		</div>
		<div class="loginSsoChooser" id="ssoChooser" style="display:none;">
			<p>Select existing account </p>
			<div class="ChooserContent">
			</div>
			<div class="linedSeparator or">
				<div class="backgroundLine"></div>
				<p>or</p>
			</div>
		</div>
		<div id="AddAppForm">
			<input  id="login" name="login" type="text" placeholder="Login"/>
			<input  id="password" name="password" type="password" placeholder="Password"/>
    	</div>
		<div class="buttonSet">
   			<button id="accept" class="btn btn-default btn-primary btn-group btn-lg">Add</button>
   			<button id="close" class="btn btn-default btn-primary btn-group btn-lg">Cancel</button>
    	</div>
</div>
</div>
