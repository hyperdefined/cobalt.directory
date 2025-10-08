<script lang="ts">
	import { page } from '$app/state';
	const currentUrl = page.url.href;
</script>

<svelte:head>
	<title>FAQ</title>
	<meta property="og:url" content={currentUrl} />
	<meta property="og:title" content="FAQ" />
	<meta property="og:description" content="Questions and answers for cobalt.directory." />
	<meta property="twitter:url" content={currentUrl} />
	<meta name="twitter:title" content="FAQ" />
	<meta name="twitter:description" content="Questions and answers for cobalt.directory." />
</svelte:head>

<h2>FAQ</h2>

<details>
	<summary>What is cobalt?</summary>
	<p>
		cobalt is an open-source media downloader. It supports a wide range of social media websites. No
		ads, tracking, or paywalls. It was created by <a href="https://github.com/imputnet/">imput</a>.
	</p>
</details>

<details>
	<summary>What is an "instance"?</summary>
	<p>
		An instance is simply another "copy" of cobalt. Because cobalt is open source, anyone can start
		up their own instance. Each entry on the tracker is a copy of cobalt, or known as an "instance."
	</p>
</details>

<details>
	<summary>What is this tracker used for?</summary>
	<p>
		This site is used to track these instances. It uses a score system to determine which community
		instance (and official one) are the best. It allows users to use other instances if the official
		one goes offline or has issues.
	</p>
	The official instance sees<i>a lot</i> of traffic, so some services may be blocked. Using other instances
	until the official is fixed is the idea. This site provides a central location to find instances.
</details>

<details>
	<summary>What is the difference between official and community instances?</summary>
	<p>
		Official instance is the main cobalt instance by the developers. This instance is <code
			>cobalt.tools</code
		>, or an API that contains <code>*.imput.net</code>. A community instance is another instance
		not ran by the official team.
	</p>
</details>

<details>
	<summary>What is the difference between API and frontend?</summary>
	<p>
		The frontend is the web app you see when you visit a cobalt instance. The API is another module
		that handles any download requests sent by the frontend. It does the processing and handling.
		When you enter a URL and download it, the frontend sends a request to the API, and it returns
		the media back.
	</p>
</details>

<h3>Instance List</h3>
<details>
	<summary>How do I read the instance list?</summary>
	<p>There's a few ways to see the instances, by the main list or by service.</p>

	<ul>
		<li>
			<a href="/" data-sveltekit-preload-data>Instance list</a>: see all cobalt instances.
		</li>
		<li>
			<a href="/service" data-sveltekit-preload-data>By service</a>: see what services work on what
			instances.
		</li>
	</ul>

	<p>
		To use an instance, simply click the link under the instance column. Or, add their API URL
		(found on the instance page, click the score) <a
			href="https://cobalt.tools/settings/instances#community">here</a
		>.
	</p>
</details>

<details>
	<summary>Why are some different colors on the list?</summary>
	<p>
		Colors are based on the score. The higher score of the instance, the more green it appears. The
		lower the score, the more red it appears.
	</p>
</details>

<details>
	<summary>I want to add/remove my instance!</summary>
	<p>
		If you want to be added/removed, ping @hyperdefined on the <a
			href="https://discord.gg/pQPt8HBUPu">cobalt discord</a
		>
		or create a pull request <a href="https://github.com/hyperdefined/cobalt.directory">here</a>.
	</p>
</details>
