# Tetris_ISU
## Purpose
To create a viable tetris clone in order to complete the ISC3U culminating assingment, and to entertain myself when wifi is down. 

## Caution
1. Sound files are not included here
2. Music files are not included here
>add them by using the guide below

## Core Features
1. Modern tetris ruleset
2. SRS kick table
3. All mini+ spin detection rule
4. custom settings(DAS, ARR, Volume)

## Current Issues
1. slow loading time into game
2. audio mismatch and latency
3. Odd behaviour when DAS = 0ms
4. unexpected drift(block drfits to the oppsite direction of input)
> Issue 4 might be a skill issue, if it persists even under slower DAS & ARR, please tell me.

## Guides
### Adding Sound effects
   Step 1
    
  1. Find Your Sound effect files, there should be 6 files in total.
    
  2. Format them into .wav files using ffmpeg or an online source
    
  Step 2

  rename them to
    
        "spin_fixed.wav", "hard_drop_fixed.wav","allclear_fixed.wav","clearline_fixed.wav","clearquad_fixed.wav","clearspin_fixed.wav"
### Adding Music
  Step 1
  1. create a folder called songs
     
  2. add the .wav files you want into the folder
