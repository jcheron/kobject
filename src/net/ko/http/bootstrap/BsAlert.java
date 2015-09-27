package net.ko.http.bootstrap;

public class BsAlert {
	private String mask = "<div class='alert alert-{role}' role='alert'>{dismiss}{content}</div>";
	private String dismissButton = "<button type='button' class='close' data-dismiss='alert' aria-label='Close'><span aria-hidden='true'>&times;</span></button>";
	private boolean dismissible;
	private String role;
	private CharSequence content;

	public BsAlert(String content) {
		this("info", content, true);
	}

	public BsAlert(String role, String content) {
		this(role, content, true);
	}

	public BsAlert(String role, String content, boolean dismissible) {
		this.role = role;
		this.content = content;
		this.dismissible = dismissible;
	}

	@Override
	public String toString() {
		return generate();
	}

	private String generate() {
		String mask = this.mask;
		String css = role;
		if (dismissible) {
			css += " alert-dismissible";
			mask = mask.replace("{dismiss}", dismissButton);
		} else
			mask = mask.replace("{dismiss}", "");
		return mask.replace("{role}", css).replace("{content}", content);
	}

	public boolean isDismissible() {
		return dismissible;
	}

	public void setDismissible(boolean dismissible) {
		this.dismissible = dismissible;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public CharSequence getContent() {
		return content;
	}

	public void setContent(CharSequence content) {
		this.content = content;
	}

	public static BsAlert info(String content) {
		return new BsAlert("info", content);
	}

	public static BsAlert success(String content) {
		return new BsAlert("success", content);
	}

	public static BsAlert warning(String content) {
		return new BsAlert("warning", content);
	}

	public static BsAlert danger(String content) {
		return new BsAlert("danger", content);
	}
}
