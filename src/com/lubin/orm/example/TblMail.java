package com.lubin.orm.example;

import java.sql.Timestamp;

import com.lubin.orm.ActiveRecord;
import com.lubin.orm.annotation.AutoIncrement;
import com.lubin.orm.annotation.PrimaryKey;



/**
 * TblMail entity. @author MyEclipse Persistence Tools
 */

public class TblMail extends ActiveRecord<TblMail> implements java.io.Serializable {

	// Fields
	@PrimaryKey
	@AutoIncrement
	private Long mail_id;
	private Integer folder_id;
	private String sender;
	private String receiver;
	private String reply_to;
	private String cc;
	private String bcc;
	private String subject;
	private String star_flag;
	private String attach_flag;
	private String read_flag;
	private String urgent_flag;
	private String receipt_flag;
	private String mail_server_uuid;
	private Integer user_mail_id;
	private Timestamp create_time;
	private Integer create_user;
	private Timestamp lastupdate_time;
	private Integer lastupdate_user;
	private Timestamp receive_time;
	private Integer email_size;
	private Integer client_id;
	private String send_status;

	// Constructors

	/** default constructor */
	public TblMail() {
	}

	/** full constructor */
	public TblMail(Integer folder_id, String sender, String receiver,
			String reply_to, String cc, String bcc, String subject,
			String star_flag, String attach_flag, String read_flag,
			String urgent_flag, String receipt_flag, String mail_server_uuid,
			Integer user_mail_id, Timestamp create_time, Integer create_user,
			Timestamp lastupdate_time, Integer lastupdate_user,
			Timestamp receive_time, Integer email_size, Integer client_id,
			String send_status) {
		this.folder_id = folder_id;
		this.sender = sender;
		this.receiver = receiver;
		this.reply_to = reply_to;
		this.cc = cc;
		this.bcc = bcc;
		this.subject = subject;
		this.star_flag = star_flag;
		this.attach_flag = attach_flag;
		this.read_flag = read_flag;
		this.urgent_flag = urgent_flag;
		this.receipt_flag = receipt_flag;
		this.mail_server_uuid = mail_server_uuid;
		this.user_mail_id = user_mail_id;
		this.create_time = create_time;
		this.create_user = create_user;
		this.lastupdate_time = lastupdate_time;
		this.lastupdate_user = lastupdate_user;
		this.receive_time = receive_time;
		this.email_size = email_size;
		this.client_id = client_id;
		this.send_status = send_status;
	}

	// Property accessors

	public Long getMail_id() {
		return this.mail_id;
	}

	public void setMail_id(Long mail_id) {
		this.mail_id = mail_id;
	}

	public Integer getFolder_id() {
		return this.folder_id;
	}

	public void setFolder_id(Integer folder_id) {
		this.folder_id = folder_id;
	}

	public String getSender() {
		return this.sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getReceiver() {
		return this.receiver;
	}

	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}

	public String getReply_to() {
		return this.reply_to;
	}

	public void setReply_to(String reply_to) {
		this.reply_to = reply_to;
	}

	public String getCc() {
		return this.cc;
	}

	public void setCc(String cc) {
		this.cc = cc;
	}

	public String getBcc() {
		return this.bcc;
	}

	public void setBcc(String bcc) {
		this.bcc = bcc;
	}

	public String getSubject() {
		return this.subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getStar_flag() {
		return this.star_flag;
	}

	public void setStar_flag(String star_flag) {
		this.star_flag = star_flag;
	}

	public String getAttach_flag() {
		return this.attach_flag;
	}

	public void setAttach_flag(String attach_flag) {
		this.attach_flag = attach_flag;
	}

	public String getRead_flag() {
		return this.read_flag;
	}

	public void setRead_flag(String read_flag) {
		this.read_flag = read_flag;
	}

	public String getUrgent_flag() {
		return this.urgent_flag;
	}

	public void setUrgent_flag(String urgent_flag) {
		this.urgent_flag = urgent_flag;
	}

	public String getReceipt_flag() {
		return this.receipt_flag;
	}

	public void setReceipt_flag(String receipt_flag) {
		this.receipt_flag = receipt_flag;
	}

	public String getMail_server_uuid() {
		return this.mail_server_uuid;
	}

	public void setMail_server_uuid(String mail_server_uuid) {
		this.mail_server_uuid = mail_server_uuid;
	}

	public Integer getUser_mail_id() {
		return this.user_mail_id;
	}

	public void setUser_mail_id(Integer user_mail_id) {
		this.user_mail_id = user_mail_id;
	}

	public Timestamp getCreate_time() {
		return this.create_time;
	}

	public void setCreate_time(Timestamp create_time) {
		this.create_time = create_time;
	}

	public Integer getCreate_user() {
		return this.create_user;
	}

	public void setCreate_user(Integer create_user) {
		this.create_user = create_user;
	}

	public Timestamp getLastupdate_time() {
		return this.lastupdate_time;
	}

	public void setLastupdate_time(Timestamp lastupdate_time) {
		this.lastupdate_time = lastupdate_time;
	}

	public Integer getLastupdate_user() {
		return this.lastupdate_user;
	}

	public void setLastupdate_user(Integer lastupdate_user) {
		this.lastupdate_user = lastupdate_user;
	}

	public Timestamp getReceive_time() {
		return this.receive_time;
	}

	public void setReceive_time(Timestamp receive_time) {
		this.receive_time = receive_time;
	}

	public Integer getEmail_size() {
		return this.email_size;
	}

	public void setEmail_size(Integer email_size) {
		this.email_size = email_size;
	}

	public Integer getClient_id() {
		return this.client_id;
	}

	public void setClient_id(Integer client_id) {
		this.client_id = client_id;
	}

	public String getSend_status() {
		return this.send_status;
	}

	public void setSend_status(String send_status) {
		this.send_status = send_status;
	}

}