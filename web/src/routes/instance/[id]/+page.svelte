<script lang="ts">
  import type { PageData } from './$types';
  import { slugify } from '$lib/utils/slug';
  import { page } from '$app/state';

  const siteUrl = page.url.origin;
  const currentUrl = page.url.href;

  export let data: PageData;

  const safeHost = (h?: string | null) => h?.replace(/^https?:\/\//, '') ?? '';
  const fmtTime = (iso: string) => {
    const d = new Date(iso);
    const pad = (n: number) => String(n).padStart(2, '0');
    const months = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];
    return `${d.getUTCFullYear()}-${months[d.getUTCMonth()]}-${pad(d.getUTCDate())} ${pad(d.getUTCHours())}:${pad(d.getUTCMinutes())}:${pad(d.getUTCSeconds())}`;
  };

  const rowClass = (online: boolean, ok: boolean) =>
    !online ? 'rating-offline' : ok ? 'rating-working' : 'rating-not-working';
</script>

<svelte:head>
  <title>{data.notFound ? 'Instance not found' : `${data.instance.titleHost}${data.instance.apiNick ? ` (${data.instance.apiNick})` : ''}`}</title>
  <meta property="og:url" content={currentUrl} />
	<meta property="og:title" content="{data.notFound ? 'Instance not found' : `${data.instance.titleHost}${data.instance.apiNick ? ` (${data.instance.apiNick})` : ''}`}" />
	<meta property="og:description" content="cobalt instance test results for {data.notFound ? 'Instance not found' : `${data.instance.titleHost}${data.instance.apiNick ? ` (${data.instance.apiNick})` : ''}`}." />
	<meta property="twitter:url" content={currentUrl} />
	<meta name="twitter:title" content="{data.notFound ? 'Instance not found' : `${data.instance.titleHost}${data.instance.apiNick ? ` (${data.instance.apiNick})` : ''}`}" />
	<meta name="twitter:description" content="cobalt instance test results for {data.notFound ? 'Instance not found' : `${data.instance.titleHost}${data.instance.apiNick ? ` (${data.instance.apiNick})` : ''}`}." />
</svelte:head>

{#if data.notFound}
  <div>
    <h2>Instance not found</h2>
    <p>No instance with id <code>{data.id}</code> was found.</p>
  </div>
{:else}
  <div>
    <h2>
      {data.instance.titleHost}
      {#if data.instance.apiNick} <span>({data.instance.apiNick})</span> {/if}
    </h2>

    <p>
      The instance ID is <code>{data.id}</code>. You can bookmark this page to see service status in the future for this instance.
    </p>

    <div style="margin: 0.5rem 0 1rem;">
      {#if data.instance.frontend && data.instance.online}
        <a href={`https://${safeHost(data.instance.frontend)}`} target="_blank" rel="noopener"><button>Use Instance</button></a>
      {:else}
        <button class="button">Use Instance</button>
      {/if}
    </div>

    <p>
      This table shows what services work for{' '}
      <code>{data.instance.titleHost}
        {#if data.instance.apiNick}
          {' '}({data.instance.apiNick})
        {/if}
      </code>.
      API URL for this instance is <code>https://{data.instance.apiHost}</code>.
    </p>

    <p>Last updated (UTC): {fmtTime(data.lastUpdatedUTC)}</p>

    <div class="table-container">
      <table class="service-table">
        <thead>
          <tr>
            <th>Service</th>
            <th>Working?</th>
            <th>Status</th>
          </tr>
        </thead>
        <tbody>
          {#each data.instance.services as s}
            {#key s.key}
              <tr class={rowClass(data.instance.online, s.status)}>
                <td>
                  {#if s.key.toLowerCase() === 'frontend'}
                    <!-- Frontend is included, but not a link -->
                    {s.friendly}
                  {:else}
                    <a href={`/service/${slugify(s.key)}`}>{s.friendly}</a>
                  {/if}
                </td>
                <td>{data.instance.online ? (s.status ? '✅' : '❌') : '❌'}</td>
                <td>{data.instance.online ? s.message : 'Offline'}</td>
              </tr>
            {/key}
          {/each}
        </tbody>
      </table>
    </div>
  </div>
{/if}