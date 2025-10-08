<script lang="ts">
	import { page } from '$app/state';
	import ClickToCopy from '$lib/components/ClickToCopy.svelte';
	const currentUrl = page.url.href;
</script>

<svelte:head>
	<title>API</title>
	<meta property="og:url" content={currentUrl} />
	<meta property="og:title" content="API" />
	<meta property="og:description" content="cobalt.directory's public API." />
	<meta property="twitter:url" content={currentUrl} />
	<meta name="twitter:title" content="API" />
	<meta name="twitter:description" content="cobalt.directory's public API." />
</svelte:head>

<h2>API</h2>
<p>
	cobalt.directory has a public API. You can use this to query the instances and the service
	results.
</p>
<p>
	Each API endpoint follows the same structure. They all have <code>lastUpdatedUTC</code> to show
	when it was last updated. All data in the endpoint is in the <code>data</code> key.
</p>

<h3>Tests Results</h3>
<ClickToCopy text="https://cobalt.directory/api/tests" /><br /><br />
<p>
	This endpoint lists all instances and what tests we ran for each one. In each test, it shows what
	the result was.
</p>
<p>You can use this to query all instances at once to see their tests and data.</p>

<h3>"Working" Instances</h3>
<p>
	This endpoint shows which instances work for what service, but in a simpler way. You can query to
	show the APIs or frontends in the list.
</p>

<ClickToCopy text="https://cobalt.directory/api/working?type=frontends" /><br />
<ClickToCopy text="https://cobalt.directory/api/working?type=api" /><br />
