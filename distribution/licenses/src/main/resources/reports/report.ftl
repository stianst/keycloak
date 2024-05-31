<html>
<body>
<style>
    body { font-size: 1em; }
    table td { font-family: monospace; }
    table, th, td { border: 1px solid black; border-collapse: collapse; }
    th, td { vertical-align: top; padding: 5px; }
    div { max-height: 200px; overflow: auto; }
    tr.bad td { background-color: #F5B7B1; }
    pre { border: 1px solid black; max-height: 300px; max-width: 1024px; overflow: auto; padding: 5px; text-wrap: wrap;
</style>

<h1>Third-party licenses</h1>

<p>
    Except as noted below, the contents of Keycloak binary releases are (1) licensed under Apache-2.0, (2) covered by
    licensing terms included in the release, (3) covered by terms having no notice or other license compliance
    obligations, or (4) covered by terms duplicative of license texts contained in this file.
</p>

<table>
    <tr>
        <th>Name</th>
        <th>Identifier</th>
        <th>Dependencies</th>
    </tr>

    <#list licenseMap as license, dependencies>
        <#if license.fsfLibre || license.osiApproved || license.cncfApproved>
            <tr>
        <#else>
            <tr class="bad">
        </#if>

        <td><a href="#${license.licenseId}">${license.name}</a></td>
        <td>${license.licenseId}</td>

        <td>
            <div>
                <#list dependencies as dependency>
                    ${dependency}
                    <#if spdxLicenses.getSourceUrl(dependency)?has_content>
                        (<a href="${spdxLicenses.getSourceUrl(dependency)}">source</a>)
                    </#if>
                    <br/>
                </#list>
            </div>
        </td>

        </tr>
    </#list>
</table>

<#list licenseMap as license, dependencies>
    <h1><a name="${license.licenseId}">${license.name}</a></h1>
    <h2>Short identifier</h2>
    ${license.licenseId}
    <h2>Other web pages for this license</h2>
    <ul>
        <li><a href="${license.reference}">${license.reference}</a></li>
        <#if license.seeAlso?has_content>
            <#list license.seeAlso as seeAlso>
                <li><a href="${seeAlso}">${seeAlso}</a></li>
            </#list>
        </#if>
    </ul>
    </h2>
    <h2>Text</h2>
    <pre>${licenseContent[license.licenseId]}</pre>

</#list>

</body>
</html>