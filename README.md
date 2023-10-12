https://voparis-wiki.obspm.fr/display/VES/ImageJ

The current version of ImageJ is v 1.53, which is slightly different from v1.51 previously used. The previous plugin no longer works with the current version.
Version 1.53t (Aug 2022) is used for this test.
Installation procedure

    Install a recent version of ImageJ on your computer (see https://imagej.nih.gov/ij/index.html)
    For Mac OS, the version "bundled with Java 1.8.0_101" is OK (requires OS X 10.8 or later)
    Plugin files will be distributed on the VESPA github when finalized — https://github.com/epn-vespa

    v 1.53 on Mac is now a bundle (ie an integrated directory which appears as a single file in the Finder, but can be opened from the local menu)

    Copy file
      SAMP_HUB-0.1.0-SNAPSHOT.jar
    in directory ImageJ/plugins  (same version number, but use the Dec 2022 version!)

    Copy files (are updates available ?)
      jsamp-1.3.5.jar
      nom-tam-fits-1.17.jar
      commons-compress-1.14.jar
    in directory ImageJ/plugins/jar

    Add these lines in file ImageJ/macros/RunAtStartup.ijm:
    //showMessage("Hi", "SAMP hub is lauching");
    run ("SAMP"); 

The plugin can also be used with recent versions of AstroImageJ (test on v5.1.3). The installation is identical.
Usage

Launch ImageJ on your machine. The SAMP Hub should start automatically and ImageJ will receive images sent there by other VO applications.  Click on new menu item SAMP > SAMP to start the Hub again if you've closed it manually.
ImageJ will accept fits, jpeg, png, and tiff images, among others. More fits images are now supported, including multi-extension files. Tiff images are prime products in some EPN-TAP services (from digitized photographic plates, e.g., BDIP) but are not usually viewable in a web browser.

For the time being, ImageJ cannot send back images to the Hub. Results have to be stored on the user machine in a VO compliant format (eg, fits, jpg, or png), and possibly loaded in another environment.
To Do

- Check functioning with v1.54 (Jan 2023) and various OS

- Support compressed images such as *.fts.bz2 from CLIMSO, which are supported in Aladin

- Support VOtables in I/O to store analysis results and load them. The current .xls output files can be loaded in TOPCAT as csv format.

- Grab the cursor position and send it to / from Aladin via SAMP

- Send back images via SAMP
