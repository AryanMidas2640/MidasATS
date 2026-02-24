package com.midas.consulting.controller.v1.response.microsoft.email;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class Value {
    private String odataEtag;
    private String id;
    private String createdDateTime;
    private String lastModifiedDateTime;
    private String changeKey;
    private List<Object> categories = new ArrayList<Object>();
    private String receivedDateTime;
    private String sentDateTime;
    private Boolean hasAttachments;
    private String internetMessageId;
    private String subject;
    private String bodyPreview;
    private String importance;
    private String parentFolderId;
    private String conversationId;
    private String conversationIndex;
    private Boolean isDeliveryReceiptRequested;
    private Boolean isReadReceiptRequested;
    private Boolean isRead;
    private Boolean isDraft;
    private String webLink;
    private String inferenceClassification;
    private Body body;
    private Sender sender;
    private From from;
    private List<ToRecipient> toRecipients = new ArrayList<ToRecipient>();
    private List<Object> ccRecipients = new ArrayList<Object>();
    private List<Object> bccRecipients = new ArrayList<Object>();
    private List<Object> replyTo = new ArrayList<Object>();
    private Flag flag;
    public String getOdataEtag() {
        return odataEtag;
    }
    public void setOdataEtag(String odataEtag) {
        this.odataEtag = odataEtag;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getCreatedDateTime() {
        return createdDateTime;
    }
    public void setCreatedDateTime(String createdDateTime) {
        this.createdDateTime = createdDateTime;
    }
    public String getLastModifiedDateTime() {
        return lastModifiedDateTime;
    }
    public void setLastModifiedDateTime(String lastModifiedDateTime) {
        this.lastModifiedDateTime = lastModifiedDateTime;
    }
    public String getChangeKey() {
        return changeKey;
    }
    public void setChangeKey(String changeKey) {
        this.changeKey = changeKey;
    }
    public List<Object> getCategories() {
        return categories;
    }
    public void setCategories(List<Object> categories) {
        this.categories = categories;
    }
    public String getReceivedDateTime() {
        return receivedDateTime;
    }
    public void setReceivedDateTime(String receivedDateTime) {
        this.receivedDateTime = receivedDateTime;
    }
    public String getSentDateTime() {
        return sentDateTime;
    }
    public void setSentDateTime(String sentDateTime) {
        this.sentDateTime = sentDateTime;
    }
    public Boolean getHasAttachments() {
        return hasAttachments;
    }
    public void setHasAttachments(Boolean hasAttachments) {
        this.hasAttachments = hasAttachments;
    }
    public String getInternetMessageId() {
        return internetMessageId;
    }
    public void setInternetMessageId(String internetMessageId) {
        this.internetMessageId = internetMessageId;
    }
    public String getSubject() {
        return subject;
    }
    public void setSubject(String subject) {
        this.subject = subject;
    }
    public String getBodyPreview() {
        return bodyPreview;
    }
    public void setBodyPreview(String bodyPreview) {
        this.bodyPreview = bodyPreview;
    }
    public String getImportance() {
        return importance;
    }
    public void setImportance(String importance) {
        this.importance = importance;
    }
    public String getParentFolderId() {
        return parentFolderId;
    }
    public void setParentFolderId(String parentFolderId) {
        this.parentFolderId = parentFolderId;
    }
    public String getConversationId() {
        return conversationId;
    }
    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
    public String getConversationIndex() {
        return conversationIndex;
    }
    public void setConversationIndex(String conversationIndex) {
        this.conversationIndex = conversationIndex;
    }
    public Boolean getIsDeliveryReceiptRequested() {
        return isDeliveryReceiptRequested;
    }
    public void setIsDeliveryReceiptRequested(Boolean isDeliveryReceiptRequested) {
        this.isDeliveryReceiptRequested = isDeliveryReceiptRequested;
    }
    public Boolean getIsReadReceiptRequested() {
        return isReadReceiptRequested;
    }
    public void setIsReadReceiptRequested(Boolean isReadReceiptRequested) {
        this.isReadReceiptRequested = isReadReceiptRequested;
    }
    public Boolean getIsRead() {
        return isRead;
    }
    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }
    public Boolean getIsDraft() {
        return isDraft;
    }
    public void setIsDraft(Boolean isDraft) {
        this.isDraft = isDraft;
    }
    public String getWebLink() {
        return webLink;
    }
    public void setWebLink(String webLink) {
        this.webLink = webLink;
    }
    public String getInferenceClassification() {
        return inferenceClassification;
    }
    public void setInferenceClassification(String inferenceClassification) {
        this.inferenceClassification = inferenceClassification;
    }
    public Body getBody() {
        return body;
    }
    public void setBody(Body body) {
        this.body = body;
    }
    public Sender getSender() {
        return sender;
    }
    public void setSender(Sender sender) {
        this.sender = sender;
    }
    public From getFrom() {
        return from;
    }
    public void setFrom(From from) {
        this.from = from;
    }
    public List<ToRecipient> getToRecipients() {
        return toRecipients;
    }
    public void setToRecipients(List<ToRecipient> toRecipients) {
        this.toRecipients = toRecipients;
    }
    public List<Object> getCcRecipients() {
        return ccRecipients;
    }
    public void setCcRecipients(List<Object> ccRecipients) {
        this.ccRecipients = ccRecipients;
    }
    public List<Object> getBccRecipients() {
        return bccRecipients;
    }
    public void setBccRecipients(List<Object> bccRecipients) {
        this.bccRecipients = bccRecipients;
    }
    public List<Object> getReplyTo() {
        return replyTo;
    }
    public void setReplyTo(List<Object> replyTo) {
        this.replyTo = replyTo;
    }
    public Flag getFlag() {
        return flag;
    }
    public void setFlag(Flag flag) {
        this.flag = flag;
    }
}