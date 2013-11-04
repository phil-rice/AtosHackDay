<html>
<head><title>Email for $email$</title>
<style type="text/css">
			html { height: 100% }
			body { height: 100%; margin: 0;
			padding: 0 }
			#map-canvas { height: 100% }
			table {
	table-layout: fixed;
	background-color: #ffffff;
	position: relative;
	border-collapse: collapse;
	margin: 0px;
	padding: 10px;
	border: 0px;
}
th {
	vertical-align: top;
	margin: 0px;
	padding: 5px;
	border: 1px solid black
}
td {
	vertical-align: top;
	margin: 0px;
	padding: 5px;
	border: 1px solid black
}
div{
   margin: 5px;
   padding: 10px;
}
</style>
</head>
<body>
To:  $email$<br />
From: GPS<br />
Subject: Latest Business Opportunities<br />
<br />

<div>Dear $contactName$,<br />
<br />
<b>New tenders that may be of interest to you</b><br />
<br />
<table><tr><th>Description</td><th>OrganisationName</th><th>Total Contract Value</th><th>Response deadline</th></tr>
$newTenders: {t |<tr><td><a href='$t.URL$'/>$t.NoticeTitle$</a></td><td>$t.NoticeOrganisationName$ </td><td>$t.TotalCapitalCost$</td><td>$t.ApproachDate$</td></td></tr>
}$</table>
<br />
<br />
<b>Changed tenders that may be of interest to you</b>
<br />
<br />
<table><tr><th>Description</td><th>OrganisationName</th><th>Total Contract Value</th><th>Response deadline</th><th>What has Changed, Old -> New</th></tr>
$changedMap: {t |<tr><td><a href='$t.URL$'/>$t.NoticeTitle$</a></td><td>$t.NoticeOrganisationName$ </td><td>$t.TotalCapitalCost$</td><td>$t.ApproachDate$</td><td>$t.changed:{ c | $c$ }$</td></tr>
}$</table>
<br />
<br />
These results are based around your saved preferences, <a href='https://online.contractsfinder.businesslink.gov.uk/'> click here to refine the types of contract we send to you.</a> 

</div>
</body>
</html>