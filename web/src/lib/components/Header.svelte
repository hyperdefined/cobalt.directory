<script lang="ts">
  import { page } from '$app/state';
  const siteUrl = page.url.origin;
</script>

<header>
    <div class="navbar-icon">
        <img src="https://raw.githubusercontent.com/imputnet/cobalt/main/web/static/icons/generic.png" alt="cobalt icon">
    </div>
    <h1>cobalt.directory</h1>
    <p><a href="{siteUrl}">Instances</a> - <a href="{siteUrl}/service">Search By Service</a> - <a href="{siteUrl}/about">About</a> - <a href="{siteUrl}/faq">FAQ</a> - <a href="https://github.com/hyperdefined/cobalt.directory">Source Code</a></p>
</header>

<style>
    header {
        background-color: rgb(25, 25, 25);
        border-bottom: 3px solid;
        border-image: linear-gradient(
            to right,
            red,
            orange,
            yellow,
            green,
            blue,
            indigo,
            violet
        )
        1;
    }
    header h1 {
        padding-bottom: 10px;
    }
    .navbar-icon {
        width: 155px;
        height: 155px;
        overflow: hidden;
        border-radius: 50%;
    }
</style>