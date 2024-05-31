<html>
<body>
<style>
    body { font-family: monospace; font-size: 10px; }
    table, th, td { border: 1px solid black; border-collapse: collapse; }
    th, td { vertical-align: top; padding: 5px; }
    div { max-height: 200px; overflow: auto; }
    tr.bad td { background-color: #F5B7B1; }
    pre { border: 1px solid black; max-height: 300px; max-width: 1024px; overflow: auto; padding: 5px; text-wrap: wrap;
</style>

<h1>Licenses</h1>
<table>
    <tr>
        <th>Name</th>
        <th>Identifier</th>
        <th>SPDX Listed?</th>
        <th>FSF Free/Libre?</th>
        <th>OSI Approved?</th>
        <th>CNCF Approved?</th>
        <th>Dependencies</th>
    </tr>

    <#list licenseMap as license, dependencies>
        <#if license.fsfLibre || license.osiApproved || license.cncfApproved>
            <tr>
        <#else>
            <tr class="bad">
        </#if>

        <#if license.licenseReference?has_content>
            <td>
                <#assign referencedLicense=spdxLicenses.findByLicenseId(license.licenseReference)>
                <a href="${license.reference}">${license.name}</a>
                (<a href="${referencedLicense.reference}">${referencedLicense.name}</a>)
            </td>
            <td>${license.licenseId} (${license.licenseReference})</td>
        <#else>
            <td><a href="${license.reference}">${license.name}</a></td>
            <td>${license.licenseId}</td>
        </#if>

        <td><#if license.spdxListed>Y</#if></td>
        <td><#if license.fsfLibre>Y</#if></td>
        <td><#if license.osiApproved>Y</#if></td>
        <td><#if license.cncfApproved>Y</#if></td>
        <td>
            <div>
                <#list dependencies as dependency>
                    ${dependency}<br/>
                </#list>
            </div>
        </td>
        </tr>
    </#list>
</table>

</body>
</html>