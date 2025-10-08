<script lang="ts">
  import type { PageData } from './$types';
  import OfficialBlurb from '$lib/components/OfficialBlurb.svelte';
  import CommunityBlurb from '$lib/components/CommunityBlurb.svelte';
  export let data: PageData;
  import { page } from '$app/state';
  import { fmtTime } from '$lib/utils/time';

  const siteUrl = page.url.origin;

  const { official, community } = data;

  const fmtServices = (i: any) => `${i.totals.up}/${i.totals.total}`;
  const safeHost = (h?: string) => h?.replace(/^https?:\/\//, '') ?? '—';

  // score -> rating class; offline wins
  const ratingClass = (i: any) => {
    if (!i.online) return 'rating-offline';
    const s = i.scorePct;
    if (s === 100) return 'rating-working';
    if (s === 0) return 'rating-bad';
    if (s >= 1 && s < 25) return 'rating-medium';
    if (s >= 25 && s < 50) return 'rating-partial';
    if (s >= 50 && s < 75) return 'rating-decent';
    if (s >= 75 && s < 100) return 'rating-good';
    return '';
  };
</script>

<svelte:head>
	<meta name="description" content="An unofficial site to track cobalt instances that are safe to use & what services work on them." />

  <meta property="og:url" content={siteUrl} />
  <meta property="og:title" content="cobalt.directory" />
	<meta property="og:description" content="An unofficial site to track cobalt instances that are safe to use & what services work on them." />

  <meta property="twitter:url" content={siteUrl} />
	<meta name="twitter:title" content="cobalt.directory" />
	<meta name="twitter:description" content="An unofficial site to track cobalt instances that are safe to use & what services work on them." />
</svelte:head>

<div>
  <!-- OFFICIAL -->
  <section>
    <p>cobalt.directory is <strong>unofficial</strong> site to track cobalt instances that are safe to use & what services work on them. This site updates every ~10 minutes.</p>
    <p>There are 2 lists, official and community below. You can also <a href="{siteUrl}/service">search by service</a>, <a href="{siteUrl}/faq">read the FAQ</a>, <a href="{siteUrl}/about">or learn about the site</a>.</p>
    <p>Last updated (UTC): {fmtTime(data.lastUpdatedUTC)}</p>
    <h2>Official Instances</h2>
    <OfficialBlurb />

    <div class="table-container">
      <table class="service-table">
        <thead>
          <tr>
            <th>Frontend</th>
            <th>API</th>
            <th>Version</th>
            <th>Remote</th>
            <th>Services</th>
            <th>Score</th>
          </tr>
        </thead>
        <tbody>
          {#if official.length === 0}
            <tr><td colspan="6">No official instances found.</td></tr>
          {:else}
            {#each official as i}
              <tr class={ratingClass(i)}>
                <td>
                    {#if i.frontend === null || i.frontend === undefined || i.frontend.trim() === ''}
                        None
                    {:else}
                        {#if i.online}
                        <a
                            href={`https://${safeHost(i.frontend)}`}
                            target="_blank"
                            rel="noopener"
                        >
                            {safeHost(i.frontend)}
                        </a>
                        {:else}
                        {safeHost(i.frontend)}
                        {/if}
                    {/if}
                </td>
                <td>{i.api ? safeHost(i.api) : '—'}</td>
                <td>{i.version ?? '—'}</td>
                <td>{i.remote ?? '—'}</td>
                <td>{fmtServices(i)}</td>
                <td>
                    {#if i.online && i.id}
                      <a href={`/instance/${i.id}`}>{i.scorePct}%</a>
                    {:else if !i.online}
                      Offline
                    {:else}
                      {i.scorePct}%
                    {/if}
                </td>
              </tr>
            {/each}
          {/if}
        </tbody>
      </table>
    </div>
  </section>

  <!-- COMMUNITY -->
  <section>
    <h2>Community Instances</h2>
    <CommunityBlurb />

    <div class="table-container">
      <table class="service-table">
        <thead>
          <tr>
            <th>Frontend</th>
            <th>API</th>
            <th>Version</th>
            <th>Remote</th>
            <th>Services</th>
            <th>Score</th>
          </tr>
        </thead>
        <tbody>
          {#if community.length === 0}
            <tr><td colspan="6">No community instances found.</td></tr>
          {:else}
            {#each community as i}
              <tr class={ratingClass(i)}>
                <td>
                    {#if i.frontend === null || i.frontend === undefined || i.frontend.trim() === ''}
                        None
                    {:else}
                        {#if i.online}
                        <a
                            href={`https://${safeHost(i.frontend)}`}
                            target="_blank"
                            rel="noopener"
                        >
                            {safeHost(i.frontend)}
                        </a>
                        {:else}
                        {safeHost(i.frontend)}
                        {/if}
                    {/if}
                </td>
                <td>{i.api ? safeHost(i.api) : '—'}</td>
                <td>{i.version ?? '—'}</td>
                <td>{i.remote ?? '—'}</td>
                <td>{fmtServices(i)}</td>
                <td>
                    {#if i.online && i.id}
                      <a href={`/instance/${i.id}`}>{i.scorePct}%</a>
                    {:else if !i.online}
                      Offline
                    {:else}
                      {i.scorePct}%
                    {/if}
                </td>
              </tr>
            {/each}
          {/if}
        </tbody>
      </table>
    </div>
  </section>
</div>