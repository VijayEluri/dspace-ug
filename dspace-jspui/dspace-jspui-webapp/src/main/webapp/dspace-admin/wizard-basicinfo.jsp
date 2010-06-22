<%--
  - wizard-questions.jsp
  -
  - Version: $Revision: 1947 $
  -
  - Date: $Date: 2007-05-18 06:50:29 -0700 (Fri, 18 May 2007) $
  -
  - Copyright (c) 2002, Hewlett-Packard Company and Massachusetts
  - Institute of Technology.  All rights reserved.
  -
  - Redistribution and use in source and binary forms, with or without
  - modification, are permitted provided that the following conditions are
  - met:
  -
  - - Redistributions of source code must retain the above copyright
  - notice, this list of conditions and the following disclaimer.
  -
  - - Redistributions in binary form must reproduce the above copyright
  - notice, this list of conditions and the following disclaimer in the
  - documentation and/or other materials provided with the distribution.
  -
  - - Neither the name of the Hewlett-Packard Company nor the name of the
  - Massachusetts Institute of Technology nor the names of their
  - contributors may be used to endorse or promote products derived from
  - this software without specific prior written permission.
  -
  - THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  - ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  - LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  - A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
  - HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  - INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  - BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
  - OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  - ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
  - TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
  - USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
  - DAMAGE.
  --%>

<%--
  - basic info for collection creation wizard
  -
  - attributes:
  -    collection - collection we're creating
  --%>

<%@ page contentType="text/html;charset=UTF-8" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"
    prefix="fmt" %>


<%@ taglib uri="http://www.dspace.org/dspace-tags.tld" prefix="dspace" %>

<%@ page import="javax.servlet.jsp.jstl.fmt.LocaleSupport" %>

<%@ page import="org.dspace.app.webui.servlet.admin.CollectionWizardServlet" %>
<%@ page import="org.dspace.content.Collection" %>

<%  Collection collection = (Collection) request.getAttribute("collection"); %>

<dspace:layout locbar="off"
               navbar="off"
               titlekey="jsp.dspace-admin.wizard-basicinfo.title"
               nocache="true">

<table width="95%">
    <tr>
      <td>
        <%-- <h1>Describe the Collection</h1> --%>
        <h1><fmt:message key="jsp.dspace-admin.wizard-basicinfo.title"/></h1>
      </td>
      <td class="standard" align="right">
        <dspace:popup page="<%= LocaleSupport.getLocalizedMessage(pageContext, \"help.site-admin\") + \"#wizard_description\"%>"><fmt:message key="jsp.help"/></dspace:popup>
      </td>
    </tr>
  </table>

    <form action="<%= request.getContextPath() %>/tools/collection-wizard" method="post" enctype="multipart/form-data">
        <table summary="Describe the Collection table">
            <tr>
            	<%-- <td><p class="submitFormLabel">Name:</p></td> --%>
            	<td><p class="submitFormLabel"><fmt:message key="jsp.dspace-admin.wizard-basicinfo.name"/></p></td>
               <td><input type="text" name="name" size="50" id="tname" /></td>
            </tr>

<%-- Hints about table width --%>
            <tr>
            	<td width="40%">&nbsp;</td>
				<td>&nbsp;</td>
				<td width="40%">&nbsp;</td>
            </tr>
            <tr>
                <%-- <td colspan="3" class="submitFormHelp">
                 Shown in list on community home page
                </td> --%>
				<td colspan="3" class="submitFormHelp">
	                <fmt:message key="jsp.dspace-admin.wizard-basicinfo.shown"/>
                </td>
            </tr>
            <tr>
                <%-- <td><p class="submitFormLabel">Short Description:</p></td> --%>
                <td><p class="submitFormLabel"><fmt:message key="jsp.dspace-admin.wizard-basicinfo.description"/></p></td>
                <td><input type="text" name="short_description" size="50"/></td>
           </tr>

            <tr><td>&nbsp;</td></tr>

            <tr>
                <td colspan="3" class="submitFormHelp">
	                <%-- HTML, shown in center of collection home page.  Be sure to enclose in &lt;P&gt; &lt;/P&gt; tags! --%>
	                <fmt:message key="jsp.dspace-admin.wizard-basicinfo.html1"/>
                </td>
            </tr>
            <tr>
                <%-- <td><p class="submitFormLabel">Introductory text:</p></td> --%>
                <td><p class="submitFormLabel"><fmt:message key="jsp.dspace-admin.wizard-basicinfo.intro"/></p></td>
                <td><textarea name="introductory_text" rows="4" cols="50"></textarea></td>
            </tr>

            <tr><td>&nbsp;</td></tr>

            <tr>
                <td colspan="3" class="submitFormHelp">
	                <%-- Plain text, shown at bottom of collection home page --%>
	                <fmt:message key="jsp.dspace-admin.wizard-basicinfo.plain"/>
                </td>
            </tr>
            <tr>
                <%-- <td><p class="submitFormLabel">Copyright text:</p></td> --%>
                <td><p class="submitFormLabel"><fmt:message key="jsp.dspace-admin.wizard-basicinfo.copyright"/></p></td>
                <td><textarea name="copyright_text" rows="3" cols="50"></textarea></td>
            </tr>

            <tr><td>&nbsp;</td></tr>

            <tr>
                <td colspan="3" class="submitFormHelp">
	                <%-- HTML, shown on right-hand side of collection home page.  Be sure to enclose in &lt;P&gt; &lt;/P&gt; tags! --%>
	                <fmt:message key="jsp.dspace-admin.wizard-basicinfo.html2"/>
                </td>
            </tr>
            <tr>
                <%-- <td><p class="submitFormLabel">Side bar text:</p></td> --%>
                <td><p class="submitFormLabel"><fmt:message key="jsp.dspace-admin.wizard-basicinfo.side"/></p></td>
                <td><textarea name="side_bar_text" rows="4" cols="50"></textarea></td>
            </tr>

            <tr><td>&nbsp;</td></tr>

            <tr>
                <td colspan="32" class="submitFormHelp">
	                <%-- Licence that submitters must grant.  Leave this blank to use the default license. --%>
	                <fmt:message key="jsp.dspace-admin.wizard-basicinfo.license1"/>
                </td>
            </tr>
            <tr>

                <%-- <td><p class="submitFormLabel">License:</p></td> --%>
                <td><p class="submitFormLabel"><fmt:message key="jsp.dspace-admin.wizard-basicinfo.license2"/></p></td>
                <td><textarea name="license" rows="4" cols="50"></textarea></td>
            </tr>

            <tr><td>&nbsp;</td></tr>

            <tr>
                <td colspan="3" class="submitFormHelp">
	                <%-- Plain text, any provenance information about this collection.  Not shown on collection pages. --%>
	                <fmt:message key="jsp.dspace-admin.wizard-basicinfo.plain2"/>

                </td>
            </tr>
            <tr>

                <%-- <td><p class="submitFormLabel">Provenance:</p></td> --%>
                <td><p class="submitFormLabel"><fmt:message key="jsp.dspace-admin.wizard-basicinfo.provenance"/></p></td>
                <td><textarea name="provenance_description" rows="4" cols="50"></textarea></td>
            </tr>

            <tr><td>&nbsp;</td></tr>

             <tr>
                <td colspan="3" class="submitFormHelp">
	                <%-- Choose a JPEG or GIF logo for the collection home page.  Should be quite small. --%>
	                <fmt:message key="jsp.dspace-admin.wizard-basicinfo.choose"/>

                </td>
            </tr>
            <tr>
                <%-- <td><p class="submitFormLabel">Logo:</p></td> --%>
                <td><p class="submitFormLabel"><fmt:message key="jsp.dspace-admin.wizard-basicinfo.logo"/></p></td>
                <td><input type="file" size="40" name="file"/></td>
            </tr>
        </table>

        <p>&nbsp;</p>

<%-- Hidden fields needed for servlet to know which collection and page to deal with --%>
        <input type="hidden" name="collection_id" value="<%= ((Collection) request.getAttribute("collection")).getID() %>" />
        <input type="hidden" name="stage" value="<%= CollectionWizardServlet.BASIC_INFO %>" />

        <center>
            <table border="0" width="80%">
                <tr>
                    <td width="100%">&nbsp;
                    </td>
                    <td>
                        <%-- <input type="submit" name="submit_next" value="Next &gt;"> --%>
                        <input type="submit" name="submit_next" value="<fmt:message key="jsp.dspace-admin.general.next.button"/>" />
                   </td>
                </tr>
            </table>
        </center>
    </form>

</dspace:layout>
