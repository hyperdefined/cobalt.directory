# cobalt.directory
A site to track community [cobalt](https://github.com/imputnet/cobalt) instances that are safe to use & what services work on them. This site runs tests on the cobalt instances in order to see how they perform and which services are working for them.

## How does it work?
It loads a list of instances, then performs various tests to see if they work. It then calculates a score on how many tests were successful.
You can see what tests it runs [here](https://github.com/hyperdefined/cobalt.directory/blob/master/backend/tests.json).

Load instances -> Make sure API/frontend exist -> Perform tests -> Build site.

## Contributing
The project contains 2 parts:
* `backend` - Module that loads and tests the instances.
* `web` - Module for building the site, using Svelte.

## How to use?
You can see a live demo at [cobalt.directory](https://cobalt.directory).

## I want to add my instance to your site!
You can:
* fork this repository, add your instance to `backend/instances`, and make a pull request.
* ping `hyperdefined` in the [cobalt discord](https://discord.gg/pQPt8HBUPu).

## License
This program is released under MIT License. See [LICENSE](https://github.com/hyperdefined/cobalt.directory/blob/master/LICENSE).