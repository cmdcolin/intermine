<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="im"%>
<%@ taglib uri="http://flymine.org/imutil" prefix="imutil"%>
<%@ taglib uri="http://jakarta.apache.org/taglibs/string-1.1"
	prefix="str"%>


<tiles:importAttribute />

<html:xhtml />
<div class="body">

<%--
<table cellpadding="0" cellspacing="0" border="0" class="dbsources">
	<c:forEach items="${subs}" var="item" varStatus="status">
		<c:if test="${status.count le 5">
			<tr>
				<td><html:link
					href="/${WEB_PROPERTIES['webapp.path']}/objectDetails.do?id=${item.value.id}">
 ${item.value.title}
    </html:link>
			</tr>
		</c:if>
	</c:forEach>
</table>
</div>

dateStyle="short"

--%>

<c:set var="LIMIT" value="5"/>
<em>${LIMIT} most recent submissions:</em>


<table cellpadding="0" cellspacing="0" border="0" class="dbsources">
  <c:forEach items="${subs}" var="item" varStatus="status">
    <c:if test="${item.key le LIMIT}">
      <tr>
        <td><html:link
          href="/${WEB_PROPERTIES['webapp.path']}/objectDetails.do?id=${item.value.id}">
 ${item.value.title}
    </html:link>
    </td>
    
    <td><fmt:formatDate value="${item.value.publicReleaseDate}" type="date" pattern="yyyy-MM-dd"/>
    </td>
    
      </tr>
    </c:if>
  </c:forEach>
</table>


<hr>


<table cellspacing="4"><tr>

<%--
		<td style="height: 80px; padding: 4px"><a
			href="http://sam.modencode.org/modweb/">
		<img src="http://sam.modencode.org/modweb/images/ark/d_s.png" class="aspectIcon"
			title="Click here to view the  Data Category" width="40px"
			height="40px" /> </a></td>

<im:querylink text="Fly" showArrow="true" showImage="fly" skipBuilder="true">

--%>
<td>		
<im:querylink text="Fly" showArrow="true" skipBuilder="true">
  <query name="" model="genomic"
    view="Submission.title Submission.design Submission.factorName Submission.factorType Submission.publicReleaseDate Submission.description"
    sortOrder="Submission.title">
  <node path="Submission" type="Submission">
  </node>
  <node path="Submission.bioEntities" type="BioEntity">
  </node>
  <node path="Submission.bioEntities.organism" type="Organism">
  <constraint op="LOOKUP" value="Drosophila melanogaster" description=""
    identifier="" code="A" extraValue="">
  </constraint>
  </node>
  </query>
</im:querylink>
		
		
				
		</td>
<td>

<im:querylink text="Worm" showArrow="true" skipBuilder="true">
  <query name="" model="genomic"
    view="Submission.title Submission.design Submission.factorName Submission.factorType Submission.publicReleaseDate Submission.description"
    sortOrder="Submission.title">
  <node path="Submission" type="Submission">
  </node>
  <node path="Submission.bioEntities" type="BioEntity">
  </node>
  <node path="Submission.bioEntities.organism" type="Organism">
  <constraint op="LOOKUP" value="Caenorhabditis elegans" description=""
    identifier="" code="A" extraValue="">
  </constraint>
  </node>
  </query>
</im:querylink>
</td>
<td>
<!-- 
<im:querylink text="   All submissions" showArrow="true" skipBuilder="true">
  <query name="" model="genomic"
    view="Submission.title Submission.design Submission.factorName Submission.factorType Submission.publicReleaseDate Submission.description"
    sortOrder="Submission.title">
  <node path="Submission" type="Submission">
  </node>
  <node path="Submission.bioEntities" type="BioEntity">
  </node>
  </query>
</im:querylink> 
-->

<html:link
          href="/${WEB_PROPERTIES['webapp.path']}/submissions.do"> All submissions <img border="0" class="arrow" src="images/right-arrow.gif" title="-&gt;"/>
    </html:link>

<br>
</td>
	</tr></table>
 






