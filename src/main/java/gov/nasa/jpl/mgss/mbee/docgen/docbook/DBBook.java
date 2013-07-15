package gov.nasa.jpl.mgss.mbee.docgen.docbook;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram;
import java.util.List;
/**
 * If you find an occasion where you want to use this, let me know.
 * 
 * @author dlam
 *
 */
public class DBBook extends DBHasContent {

	private String subtitle;
	private String acknowledgement;
	private String legalnotice;
	private Diagram coverimage;
	private boolean index;
	private String DocumentID;
	private String DocumentVersion;
	private String LogoAlignment;
	private String LogoLocation;
	private String AbbreviatedProjectName;
	private String DocushareLink;
	private String AbbreviatedTitle;
	private String TitlePageLegalNotice;
	private String FooterLegalNotice;
	private List <String> CollaboratorEmail;
	private Boolean RemoveBlankPages;
	private List <String> Author;
	private List <String> Approver;
	private List <String> Concurrance;
	private List <String> RevisionHistory;
	private String JPLProjectTitle;
	private boolean UseDefaultStylesheet;
	private String LogoSize;

	public DBBook() {
		subtitle = "";
		}
	
	
	public boolean getIndex() {
		return index;
	}

	public String getSubtitle() {
		return subtitle;
	}


	public String getAcknowledgement() {
		return acknowledgement;
	}


	public String getLegalnotice() {
		return legalnotice;
	}
	public String getDocumentID(){
		return DocumentID;
	}
	public String getDocumentVersion(){
		return DocumentVersion;
	}
	public String getLogoAlignment(){
		return LogoAlignment;
	}
	public String getLogoLocation(){
		return LogoLocation;
	}
	public String getAbbreviatedProjectName(){
		return AbbreviatedProjectName;
	}
	public String getDocushareLink(){
		return DocushareLink;
	}
	public String getLogoSize(){
		return LogoSize;
	}
	public String getAbbreviatedTitle(){
		return AbbreviatedTitle;
	}
	public String getTitlePageLegalNotice(){
		return TitlePageLegalNotice;
	}
	public String getFooterLegalNotice(){
		return FooterLegalNotice;
	}
	public List <String> getCollaboratorEmail(){
		return CollaboratorEmail;
	}
	public Boolean getRemoveBlankPages(){
		return RemoveBlankPages;
	}
	public List<String> getAuthor(){
		return Author;
	}
	public List <String> getApprover(){
		return Approver;
	}
	public List <String> getConcurrance(){
		return Concurrance;
	}
	public List <String> getRevisionHistory(){
		return RevisionHistory;
	}
	public String getJPLProjectTitle(){
		return JPLProjectTitle;
	}
	public boolean getUseDefaultStylesheet(){
		return UseDefaultStylesheet;
	}

	public Diagram getCoverimage() {
		return coverimage;
	}

	public void setIndex(boolean index) {
		this.index = index;
	}
	
	public void setSubtitle(String s) {
		subtitle = s;
	}
	
	public void setAcknowledgement(String ack) {
		acknowledgement = ack;
	}
	public void setLogoSize(String s){
		LogoSize=s;
	}
	public void setDocumentID(String s){
		DocumentID = s;
	}
	public void setDocumentVersion(String s){
		DocumentVersion=s;
	}
	public void setLogoAlignment(String s){
		LogoAlignment=s;
	}
	public void setLogoLocation(String s){
		LogoLocation=s;
	}
	public void setAbbreviatedProjectName(String s){
		AbbreviatedProjectName=s;
	}
	public void setDocushareLink(String s){
		DocushareLink=s;
	}
	public void setAbbreviatedTitle(String s){
		AbbreviatedTitle=s;
	}
	public void setTitlePageLegalNotice(String s){
		TitlePageLegalNotice=s;
	}
	public void setFooterLegalNotice(String s){
		FooterLegalNotice=s;
	}
	public void setCollaboratorEmail(List <String> s){
		CollaboratorEmail=s;
	}
	public void setRevisionHistory(List <String> s){
		RevisionHistory=s;
	}
	public void setRemoveBlankPages(Boolean s){
		RemoveBlankPages=s;
	}
	public void setAuthor(List<String> s){
		Author=s;
	}
	public void setApprover(List <String> s){
		Approver=s;
	}
	public void setConcurrance(List <String> s){
		Concurrance=s;
	}
	public void setJPLProjectTitle(String s){
		JPLProjectTitle=s;
	}
	public void setUseDefaulStylesheet(boolean s){
		UseDefaultStylesheet=s;
	}
	
	public void setCoverimage(Diagram d) {
		coverimage = d;
	}
	
	public void setLegalnotice(String l) {
		legalnotice = l;
	}

	@Override
	public void accept(IDBVisitor v) {
		v.visit(this);
	}
}
