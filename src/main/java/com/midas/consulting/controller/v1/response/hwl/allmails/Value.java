package com.midas.consulting.controller.v1.response.hwl.allmails;

import com.fasterxml.jackson.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"@odata.etag",
"id",
"createdDateTime",
"lastModifiedDateTime",
"changeKey",
"categories",
"receivedDateTime",
"sentDateTime",
"hasAttachments",
"internetMessageId",
"subject",
"bodyPreview",
"importance",
"parentFolderId",
"conversationId",
"conversationIndex",
"isDeliveryReceiptRequested",
"isReadReceiptRequested",
"isRead",
"isDraft",
"webLink",
"inferenceClassification",
"body",
"sender",
"from",
"toRecipients",
"ccRecipients",
"bccRecipients",
"replyTo",
"flag"
})
public class Value {

@JsonProperty("@odata.etag")
private String odataEtag;
@JsonProperty("id")
private String id;
@JsonProperty("createdDateTime")
private String createdDateTime;
@JsonProperty("lastModifiedDateTime")
private String lastModifiedDateTime;
@JsonProperty("changeKey")
private String changeKey;
@JsonProperty("categories")
private List<Object> categories;
@JsonProperty("receivedDateTime")
private String receivedDateTime;
@JsonProperty("sentDateTime")
private String sentDateTime;
@JsonProperty("hasAttachments")
private Boolean hasAttachments;
@JsonProperty("internetMessageId")
private String internetMessageId;
@JsonProperty("subject")
private String subject;
@JsonProperty("bodyPreview")
private String bodyPreview;
@JsonProperty("importance")
private String importance;
@JsonProperty("parentFolderId")
private String parentFolderId;
@JsonProperty("conversationId")
private String conversationId;
@JsonProperty("conversationIndex")
private String conversationIndex;
@JsonProperty("isDeliveryReceiptRequested")
private Boolean isDeliveryReceiptRequested;
@JsonProperty("isReadReceiptRequested")
private Boolean isReadReceiptRequested;
@JsonProperty("isRead")
private Boolean isRead;
@JsonProperty("isDraft")
private Boolean isDraft;
@JsonProperty("webLink")
private String webLink;
@JsonProperty("inferenceClassification")
private String inferenceClassification;
@JsonProperty("body")
private Body body;
@JsonProperty("sender")
private Sender sender;
@JsonProperty("from")
private From from;
@JsonProperty("toRecipients")
private List<ToRecipient> toRecipients;
@JsonProperty("ccRecipients")
private List<Object> ccRecipients;
@JsonProperty("bccRecipients")
private List<Object> bccRecipients;
@JsonProperty("replyTo")
private List<Object> replyTo;
@JsonProperty("flag")
private Flag flag;
@JsonIgnore
private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

@JsonProperty("@odata.etag")
public String getOdataEtag() {
return odataEtag;
}

@JsonProperty("@odata.etag")
public void setOdataEtag(String odataEtag) {
this.odataEtag = odataEtag;
}

@JsonProperty("id")
public String getId() {
return id;
}

@JsonProperty("id")
public void setId(String id) {
this.id = id;
}

@JsonProperty("createdDateTime")
public String getCreatedDateTime() {
return createdDateTime;
}

@JsonProperty("createdDateTime")
public void setCreatedDateTime(String createdDateTime) {
this.createdDateTime = createdDateTime;
}

@JsonProperty("lastModifiedDateTime")
public String getLastModifiedDateTime() {
return lastModifiedDateTime;
}

@JsonProperty("lastModifiedDateTime")
public void setLastModifiedDateTime(String lastModifiedDateTime) {
this.lastModifiedDateTime = lastModifiedDateTime;
}

@JsonProperty("changeKey")
public String getChangeKey() {
return changeKey;
}

@JsonProperty("changeKey")
public void setChangeKey(String changeKey) {
this.changeKey = changeKey;
}

@JsonProperty("categories")
public List<Object> getCategories() {
return categories;
}

@JsonProperty("categories")
public void setCategories(List<Object> categories) {
this.categories = categories;
}

@JsonProperty("receivedDateTime")
public String getReceivedDateTime() {
return receivedDateTime;
}

@JsonProperty("receivedDateTime")
public void setReceivedDateTime(String receivedDateTime) {
this.receivedDateTime = receivedDateTime;
}

@JsonProperty("sentDateTime")
public String getSentDateTime() {
return sentDateTime;
}

@JsonProperty("sentDateTime")
public void setSentDateTime(String sentDateTime) {
this.sentDateTime = sentDateTime;
}

@JsonProperty("hasAttachments")
public Boolean getHasAttachments() {
return hasAttachments;
}

@JsonProperty("hasAttachments")
public void setHasAttachments(Boolean hasAttachments) {
this.hasAttachments = hasAttachments;
}

@JsonProperty("internetMessageId")
public String getInternetMessageId() {
return internetMessageId;
}

@JsonProperty("internetMessageId")
public void setInternetMessageId(String internetMessageId) {
this.internetMessageId = internetMessageId;
}

@JsonProperty("subject")
public String getSubject() {
return subject;
}

@JsonProperty("subject")
public void setSubject(String subject) {
this.subject = subject;
}

@JsonProperty("bodyPreview")
public String getBodyPreview() {
return bodyPreview;
}

@JsonProperty("bodyPreview")
public void setBodyPreview(String bodyPreview) {
this.bodyPreview = bodyPreview;
}

@JsonProperty("importance")
public String getImportance() {
return importance;
}

@JsonProperty("importance")
public void setImportance(String importance) {
this.importance = importance;
}

@JsonProperty("parentFolderId")
public String getParentFolderId() {
return parentFolderId;
}

@JsonProperty("parentFolderId")
public void setParentFolderId(String parentFolderId) {
this.parentFolderId = parentFolderId;
}

@JsonProperty("conversationId")
public String getConversationId() {
return conversationId;
}

@JsonProperty("conversationId")
public void setConversationId(String conversationId) {
this.conversationId = conversationId;
}

@JsonProperty("conversationIndex")
public String getConversationIndex() {
return conversationIndex;
}

@JsonProperty("conversationIndex")
public void setConversationIndex(String conversationIndex) {
this.conversationIndex = conversationIndex;
}

@JsonProperty("isDeliveryReceiptRequested")
public Boolean getIsDeliveryReceiptRequested() {
return isDeliveryReceiptRequested;
}

@JsonProperty("isDeliveryReceiptRequested")
public void setIsDeliveryReceiptRequested(Boolean isDeliveryReceiptRequested) {
this.isDeliveryReceiptRequested = isDeliveryReceiptRequested;
}

@JsonProperty("isReadReceiptRequested")
public Boolean getIsReadReceiptRequested() {
return isReadReceiptRequested;
}

@JsonProperty("isReadReceiptRequested")
public void setIsReadReceiptRequested(Boolean isReadReceiptRequested) {
this.isReadReceiptRequested = isReadReceiptRequested;
}

@JsonProperty("isRead")
public Boolean getIsRead() {
return isRead;
}

@JsonProperty("isRead")
public void setIsRead(Boolean isRead) {
this.isRead = isRead;
}

@JsonProperty("isDraft")
public Boolean getIsDraft() {
return isDraft;
}

@JsonProperty("isDraft")
public void setIsDraft(Boolean isDraft) {
this.isDraft = isDraft;
}

@JsonProperty("webLink")
public String getWebLink() {
return webLink;
}

@JsonProperty("webLink")
public void setWebLink(String webLink) {
this.webLink = webLink;
}

@JsonProperty("inferenceClassification")
public String getInferenceClassification() {
return inferenceClassification;
}

@JsonProperty("inferenceClassification")
public void setInferenceClassification(String inferenceClassification) {
this.inferenceClassification = inferenceClassification;
}

@JsonProperty("body")
public Body getBody() {
return body;
}

@JsonProperty("body")
public void setBody(Body body) {
this.body = body;
}

@JsonProperty("sender")
public Sender getSender() {
return sender;
}

@JsonProperty("sender")
public void setSender(Sender sender) {
this.sender = sender;
}

@JsonProperty("from")
public From getFrom() {
return from;
}

@JsonProperty("from")
public void setFrom(From from) {
this.from = from;
}

@JsonProperty("toRecipients")
public List<ToRecipient> getToRecipients() {
return toRecipients;
}

@JsonProperty("toRecipients")
public void setToRecipients(List<ToRecipient> toRecipients) {
this.toRecipients = toRecipients;
}

@JsonProperty("ccRecipients")
public List<Object> getCcRecipients() {
return ccRecipients;
}

@JsonProperty("ccRecipients")
public void setCcRecipients(List<Object> ccRecipients) {
this.ccRecipients = ccRecipients;
}

@JsonProperty("bccRecipients")
public List<Object> getBccRecipients() {
return bccRecipients;
}

@JsonProperty("bccRecipients")
public void setBccRecipients(List<Object> bccRecipients) {
this.bccRecipients = bccRecipients;
}

@JsonProperty("replyTo")
public List<Object> getReplyTo() {
return replyTo;
}

@JsonProperty("replyTo")
public void setReplyTo(List<Object> replyTo) {
this.replyTo = replyTo;
}

@JsonProperty("flag")
public Flag getFlag() {
return flag;
}

@JsonProperty("flag")
public void setFlag(Flag flag) {
this.flag = flag;
}

@JsonAnyGetter
public Map<String, Object> getAdditionalProperties() {
return this.additionalProperties;
}

@JsonAnySetter
public void setAdditionalProperty(String name, Object value) {
this.additionalProperties.put(name, value);
}

}