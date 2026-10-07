/**
 * Talo Music — Premium Android Music Player (Interactive Prototype)
 * Local-First • Web Audio Engine • Jetpack Compose Design System Parity
 */

// Initial Mock Library Data (High-fidelity curated local tracks)
let librarySongs = [
  {
    id: 1,
    title: "Aura of Silence",
    artist: "Komorebi Echo",
    album: "Solitude Horizons",
    durationMs: 218000,
    cover: "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=500&q=80",
    isFavorite: true,
    freqBase: 220
  },
  {
    id: 2,
    title: "Velvet Horizons",
    artist: "Nara Drift",
    album: "Solitude Horizons",
    durationMs: 194000,
    cover: "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=500&q=80",
    isFavorite: false,
    freqBase: 293
  },
  {
    id: 3,
    title: "Midnight Monochrome",
    artist: "Talo Acoustics",
    album: "Pure Architecture",
    durationMs: 252000,
    cover: "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=500&q=80",
    isFavorite: true,
    freqBase: 174
  },
  {
    id: 4,
    title: "Nordic Sunset",
    artist: "Stellan Vane",
    album: "Fjord Lights",
    durationMs: 180000,
    cover: "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500&q=80",
    isFavorite: false,
    freqBase: 330
  },
  {
    id: 5,
    title: "Cascade Frequency",
    artist: "Komorebi Echo",
    album: "Pure Architecture",
    durationMs: 240000,
    cover: "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=500&q=80",
    isFavorite: false,
    freqBase: 261
  },
  {
    id: 6,
    title: "Amber Memory",
    artist: "Nara Drift",
    album: "Warm Embers",
    durationMs: 205000,
    cover: "https://images.unsplash.com/photo-1498038432885-c6f3f1b912ee?w=500&q=80",
    isFavorite: true,
    freqBase: 392
  }
];

let playlists = [
  { id: 101, name: "Calm Mornings", count: 4 },
  { id: 102, name: "Late Night Studio", count: 6 },
  { id: 103, name: "Acoustic Journey", count: 3 }
];

// Playback State
let currentQueue = [...librarySongs];
let currentTrackIndex = 0;
let isPlaying = false;
let currentPositionSec = 0;
let isShuffle = false;
let repeatMode = 0; // 0: Off, 1: All, 2: One
let progressInterval = null;

// Audio Engine (Web Audio API Synthesizer + Audio Element for files)
let audioCtx = null;
let synthOsc = null;
let synthGain = null;
let bassFilter = null;
let midFilter = null;
let trebFilter = null;
let htmlAudio = new Audio();
let isPlayingUploadedFile = false;

function initAudioContext() {
  if (audioCtx) return;
  const AudioContext = window.AudioContext || window.webkitAudioContext;
  audioCtx = new AudioContext();

  // Create 3-band Equalizer nodes
  bassFilter = audioCtx.createBiquadFilter();
  bassFilter.type = "lowshelf";
  bassFilter.frequency.value = 150;
  bassFilter.gain.value = 4;

  midFilter = audioCtx.createBiquadFilter();
  midFilter.type = "peaking";
  midFilter.frequency.value = 1000;
  midFilter.gain.value = 0;

  trebFilter = audioCtx.createBiquadFilter();
  trebFilter.type = "highshelf";
  trebFilter.frequency.value = 8000;
  trebFilter.gain.value = 2;

  // Connect EQ chain to destination
  bassFilter.connect(midFilter);
  midFilter.connect(trebFilter);
  trebFilter.connect(audioCtx.destination);
}

function playTrack(index, queue = currentQueue) {
  initAudioContext();
  if (audioCtx && audioCtx.state === 'suspended') {
    audioCtx.resume();
  }

  currentQueue = queue;
  currentTrackIndex = Math.max(0, Math.min(index, currentQueue.length - 1));
  const track = currentQueue[currentTrackIndex];
  if (!track) return;

  currentPositionSec = 0;
  isPlaying = true;

  // Stop previous synth if playing
  stopSynth();

  if (track.audioUrl) {
    isPlayingUploadedFile = true;
    htmlAudio.src = track.audioUrl;
    htmlAudio.currentTime = 0;
    htmlAudio.play().catch(e => console.log(e));
  } else {
    isPlayingUploadedFile = false;
    startSynthNote(track.freqBase || 220);
  }

  startProgressTicker();
  updateUI();
}

function startSynthNote(freq) {
  try {
    synthOsc = audioCtx.createOscillator();
    synthGain = audioCtx.createGain();

    synthOsc.type = 'sine';
    synthOsc.frequency.setValueAtTime(freq, audioCtx.currentTime);

    // Warm chord harmonic
    synthGain.gain.setValueAtTime(0.01, audioCtx.currentTime);
    synthGain.gain.exponentialRampToValueAtTime(0.15, audioCtx.currentTime + 0.8);

    synthOsc.connect(synthGain);
    synthGain.connect(bassFilter);
    synthOsc.start();
  } catch (e) {
    console.error(e);
  }
}

function stopSynth() {
  if (synthOsc) {
    try {
      synthOsc.stop();
      synthOsc.disconnect();
    } catch (e) {}
    synthOsc = null;
  }
  if (isPlayingUploadedFile) {
    htmlAudio.pause();
  }
}

function togglePlayPause() {
  initAudioContext();
  if (audioCtx && audioCtx.state === 'suspended') {
    audioCtx.resume();
  }

  if (currentQueue.length === 0) return;

  isPlaying = !isPlaying;
  const track = currentQueue[currentTrackIndex];

  if (isPlaying) {
    if (track?.audioUrl) {
      htmlAudio.play().catch(e => console.log(e));
    } else {
      startSynthNote(track ? track.freqBase : 220);
    }
    startProgressTicker();
  } else {
    stopSynth();
    clearInterval(progressInterval);
  }
  updateUI();
}

function skipNext() {
  if (currentQueue.length === 0) return;
  if (isShuffle) {
    const nextIdx = Math.floor(Math.random() * currentQueue.length);
    playTrack(nextIdx, currentQueue);
  } else {
    let nextIdx = currentTrackIndex + 1;
    if (nextIdx >= currentQueue.length) {
      if (repeatMode === 1) nextIdx = 0; // Repeat All
      else return; // Stop at end of queue
    }
    playTrack(nextIdx, currentQueue);
  }
}

function skipPrev() {
  if (currentPositionSec > 3) {
    currentPositionSec = 0;
    if (isPlayingUploadedFile) htmlAudio.currentTime = 0;
    updateUI();
  } else {
    let prevIdx = currentTrackIndex - 1;
    if (prevIdx < 0) prevIdx = currentQueue.length - 1;
    playTrack(prevIdx, currentQueue);
  }
}

function seekTo(fraction) {
  const track = currentQueue[currentTrackIndex];
  if (!track) return;
  const durationSec = track.durationMs / 1000;
  currentPositionSec = fraction * durationSec;
  if (isPlayingUploadedFile) {
    htmlAudio.currentTime = currentPositionSec;
  }
  updateUI();
}

function startProgressTicker() {
  clearInterval(progressInterval);
  progressInterval = setInterval(() => {
    if (!isPlaying) return;
    const track = currentQueue[currentTrackIndex];
    if (!track) return;

    const durationSec = track.durationMs / 1000;
    if (isPlayingUploadedFile && htmlAudio.duration) {
      currentPositionSec = htmlAudio.currentTime;
    } else {
      currentPositionSec += 0.5;
    }

    // A-B Loop repetition
    if (window.labLoopEnabled && window.labLoopEnd > window.labLoopStart) {
      if (currentPositionSec >= window.labLoopEnd || currentPositionSec < window.labLoopStart) {
        currentPositionSec = window.labLoopStart;
        if (isPlayingUploadedFile) htmlAudio.currentTime = window.labLoopStart;
      }
    }

    if (currentPositionSec >= durationSec) {
      if (repeatMode === 2) {
        // Repeat one
        playTrack(currentTrackIndex, currentQueue);
      } else {
        skipNext();
      }
    }
    updateProgressUI();
    if (window.updateLabUI) window.updateLabUI();
  }, 400);
}

function formatTime(seconds) {
  const s = Math.floor(Math.max(0, seconds));
  const m = Math.floor(s / 60);
  const remS = s % 60;
  return `${m}:${remS < 10 ? '0' : ''}${remS}`;
}

// UI Elements
const miniPlayer = document.getElementById('miniPlayer');
const miniTitle = document.getElementById('miniTitle');
const miniArtist = document.getElementById('miniArtist');
const miniThumbImg = document.getElementById('miniThumbImg');
const miniPlayIcon = document.getElementById('miniPlayIcon');
const miniProgressFill = document.getElementById('miniProgressFill');

const nowPlayingOverlay = document.getElementById('nowPlayingOverlay');
const npTitle = document.getElementById('npTitle');
const npArtist = document.getElementById('npArtist');
const npHeroImg = document.getElementById('npHeroImg');
const npHeroPlayIcon = document.getElementById('npHeroPlayIcon');
const npFavBtn = document.getElementById('npFavBtn');
const npFavIcon = document.getElementById('npFavIcon');
const npSeekSlider = document.getElementById('npSeekSlider');
const npTimeElapsed = document.getElementById('npTimeElapsed');
const npTimeTotal = document.getElementById('npTimeTotal');
const npShuffleBtn = document.getElementById('npShuffleBtn');
const npRepeatBtn = document.getElementById('npRepeatBtn');
const heroArtworkWrap = document.getElementById('heroArtworkWrap');

function updateUI() {
  const track = currentQueue[currentTrackIndex];
  if (!track) {
    miniPlayer.classList.add('hidden');
    return;
  }

  miniPlayer.classList.remove('hidden');
  miniTitle.textContent = track.title;
  miniArtist.textContent = track.artist;
  miniThumbImg.src = track.cover;

  npTitle.textContent = track.title;
  npArtist.textContent = track.artist;
  npHeroImg.src = track.cover;

  // Play/Pause Icons
  const playPath = '<path d="M8 5v14l11-7z"/>';
  const pausePath = '<path d="M6 19h4V5H6v14zm8-14v14h4V5h-4z"/>';
  miniPlayIcon.innerHTML = isPlaying ? pausePath : playPath;
  npHeroPlayIcon.innerHTML = isPlaying ? pausePath : playPath;

  // Favorite button state
  if (track.isFavorite) {
    npFavBtn.classList.add('is-fav');
    npFavIcon.style.fill = 'var(--primary)';
  } else {
    npFavBtn.classList.remove('is-fav');
    npFavIcon.style.fill = 'var(--text-tertiary)';
  }

  // Shuffle & Repeat buttons
  npShuffleBtn.classList.toggle('is-active', isShuffle);
  npRepeatBtn.classList.toggle('is-active', repeatMode > 0);

  updateProgressUI();
  renderHomeRecentSongs();
  renderLibrarySongs();
}

function updateProgressUI() {
  const track = currentQueue[currentTrackIndex];
  if (!track) return;
  const durationSec = track.durationMs / 1000;
  const fraction = Math.min(1, currentPositionSec / durationSec);

  miniProgressFill.style.width = `${fraction * 100}%`;
  npSeekSlider.value = fraction * 100;
  npTimeElapsed.textContent = formatTime(currentPositionSec);
  npTimeTotal.textContent = formatTime(durationSec);
}

// Render Home Screen Sections
function renderHomeScreen() {
  // Greeting based on time
  const hour = new Date().getHours();
  const greetingEl = document.getElementById('greetingText');
  if (hour < 12) greetingEl.textContent = 'Good morning';
  else if (hour < 18) greetingEl.textContent = 'Good afternoon';
  else greetingEl.textContent = 'Good evening';

  // Recently Played Albums
  const albumsRow = document.getElementById('homeRecentAlbums');
  const albums = [
    { title: "Solitude Horizons", artist: "Komorebi Echo", cover: librarySongs[0].cover },
    { title: "Pure Architecture", artist: "Talo Acoustics", cover: librarySongs[2].cover },
    { title: "Fjord Lights", artist: "Stellan Vane", cover: librarySongs[3].cover },
    { title: "Warm Embers", artist: "Nara Drift", cover: librarySongs[5].cover }
  ];

  albumsRow.innerHTML = albums.map(alb => `
    <div class="album-card-item" onclick="playAlbumTracks('${alb.title}')">
      <div class="album-art-wrap">
        <img src="${alb.cover}" alt="${alb.title}">
      </div>
      <div class="album-card-title">${alb.title}</div>
      <div class="album-card-artist">${alb.artist}</div>
    </div>
  `).join('');

  // Playlists row
  const playlistsRow = document.getElementById('homePlaylistsRow');
  playlistsRow.innerHTML = playlists.map(pl => `
    <div class="playlist-card-tile">
      <svg viewBox="0 0 24 24" fill="currentColor"><path d="M4 10h12v2H4zm0-4h12v2H4zm0 8h8v2H4zm10 0v6l5-3-5-3z"/></svg>
      <div class="playlist-card-name">${pl.name}</div>
    </div>
  `).join('');

  renderHomeRecentSongs();
}

function renderHomeRecentSongs() {
  const listEl = document.getElementById('homeRecentSongsList');
  listEl.innerHTML = librarySongs.slice(0, 5).map((song, i) => {
    const isThisActive = currentQueue[currentTrackIndex]?.id === song.id && isPlaying;
    return `
      <div class="song-list-item ${isThisActive ? 'is-active' : ''}" onclick="playTrackById(${song.id})">
        <div class="song-thumb">
          <img src="${song.cover}" alt="">
        </div>
        <div class="song-info-col">
          <div class="song-item-title">${song.title}</div>
          <div class="song-item-meta">${song.artist} • ${formatTime(song.durationMs / 1000)}</div>
        </div>
        <button class="icon-btn" title="Open in Music Lab" onclick="event.stopPropagation(); openMusicLab(${song.id})" style="color: var(--primary);">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M10 20h4V4h-4v16zm-6 0h4v-8H4v8zM16 9v11h4V9h-4z"/></svg>
        </button>
        <button class="icon-btn ${song.isFavorite ? 'is-fav' : ''}" onclick="event.stopPropagation(); toggleFavoriteTrack(${song.id})">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
            <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
          </svg>
        </button>
      </div>
    `;
  }).join('');

  const favCount = librarySongs.filter(s => s.isFavorite).length;
  document.getElementById('favCountSubtitle').textContent = `${favCount} favorite tracks • Tap to play`;
}

// Render Library
function renderLibrarySongs() {
  const content = document.getElementById('libTabContent');
  document.getElementById('libSummaryText').textContent = `${librarySongs.length} local audio tracks`;

  content.innerHTML = librarySongs.map(song => {
    const isThisActive = currentQueue[currentTrackIndex]?.id === song.id && isPlaying;
    return `
      <div class="song-list-item ${isThisActive ? 'is-active' : ''}" onclick="playTrackById(${song.id})">
        <div class="song-thumb">
          <img src="${song.cover}" alt="">
        </div>
        <div class="song-info-col">
          <div class="song-item-title">${song.title}</div>
          <div class="song-item-meta">${song.artist} • ${formatTime(song.durationMs / 1000)}</div>
        </div>
        <button class="icon-btn" title="Open in Music Lab" onclick="event.stopPropagation(); openMusicLab(${song.id})" style="color: var(--primary);">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M10 20h4V4h-4v16zm-6 0h4v-8H4v8zM16 9v11h4V9h-4z"/></svg>
        </button>
        <button class="icon-btn ${song.isFavorite ? 'is-fav' : ''}" onclick="event.stopPropagation(); toggleFavoriteTrack(${song.id})">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
            <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
          </svg>
        </button>
      </div>
    `;
  }).join('');
}

// Global functions for inline onclick handlers
window.playTrackById = function(id) {
  const idx = librarySongs.findIndex(s => s.id === id);
  if (idx !== -1) playTrack(idx, librarySongs);
};

window.playAlbumTracks = function(albumName) {
  const albumTracks = librarySongs.filter(s => s.album === albumName);
  if (albumTracks.length > 0) playTrack(0, albumTracks);
};

window.toggleFavoriteTrack = function(id) {
  const song = librarySongs.find(s => s.id === id);
  if (song) {
    song.isFavorite = !song.isFavorite;
    updateUI();
  }
};

// Search System (Offline Instant Filter)
const searchInput = document.getElementById('searchInput');
const searchResults = document.getElementById('searchResultsContainer');
const btnClearSearch = document.getElementById('btnClearSearch');

searchInput.addEventListener('input', (e) => {
  const query = e.target.value.trim().toLowerCase();
  btnClearSearch.style.display = query ? 'flex' : 'none';

  if (!query) {
    searchResults.innerHTML = `
      <div style="text-align: center; padding: 48px 24px; color: var(--text-tertiary);">
        <svg width="44" height="44" viewBox="0 0 24 24" fill="currentColor" style="opacity: 0.4;">
          <path d="M15.5 14h-.79l-.28-.27A6.471 6.471 0 0 0 16 9.5 6.5 6.5 0 1 0 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/>
        </svg>
        <p style="margin-top: 10px; font-weight: 600; color: var(--text-secondary);">Instant local search</p>
        <p style="font-size: 13px; margin-top: 4px;">Zero latency • 100% offline</p>
      </div>
    `;
    return;
  }

  const matchedSongs = librarySongs.filter(s => s.title.toLowerCase().includes(query) || s.artist.toLowerCase().includes(query));

  if (matchedSongs.length === 0) {
    searchResults.innerHTML = `<div style="text-align: center; padding: 40px; color: var(--text-secondary);">No tracks found for "${query}"</div>`;
    return;
  }

  searchResults.innerHTML = `
    <h3 class="section-title" style="font-size: 16px; margin-top: 12px;">Tracks (${matchedSongs.length})</h3>
    ${matchedSongs.map(song => `
      <div class="song-list-item" onclick="playTrackById(${song.id})">
        <div class="song-thumb"><img src="${song.cover}" alt=""></div>
        <div class="song-info-col">
          <div class="song-item-title">${song.title}</div>
          <div class="song-item-meta">${song.artist} • ${formatTime(song.durationMs / 1000)}</div>
        </div>
      </div>
    `).join('')}
  `;
});

btnClearSearch.addEventListener('click', () => {
  searchInput.value = '';
  searchInput.dispatchEvent(new Event('input'));
  searchInput.focus();
});

// Navigation Bottom Bar
const navButtons = document.querySelectorAll('.nav-item-btn');
const views = {
  home: document.getElementById('viewHome'),
  library: document.getElementById('viewLibrary'),
  search: document.getElementById('viewSearch'),
  settings: document.getElementById('viewSettings')
};

navButtons.forEach(btn => {
  btn.addEventListener('click', () => {
    const target = btn.getAttribute('data-nav');
    navButtons.forEach(b => b.classList.remove('active'));
    btn.classList.add('active');

    Object.keys(views).forEach(vKey => {
      views[vKey].style.display = vKey === target ? 'block' : 'none';
    });

    if (target === 'library') renderLibrarySongs();
  });
});

// Mini Player -> Now Playing Expand
miniPlayer.addEventListener('click', () => {
  nowPlayingOverlay.classList.add('active');
});

document.getElementById('btnDismissNowPlaying').addEventListener('click', () => {
  nowPlayingOverlay.classList.remove('active');
});

// Swipe Gestures on Now Playing (Swipe down to dismiss, swipe left for next, swipe right for prev)
let touchStartX = 0;
let touchStartY = 0;

heroArtworkWrap.addEventListener('touchstart', (e) => {
  touchStartX = e.changedTouches[0].screenX;
  touchStartY = e.changedTouches[0].screenY;
}, { passive: true });

heroArtworkWrap.addEventListener('touchend', (e) => {
  const deltaX = e.changedTouches[0].screenX - touchStartX;
  const deltaY = e.changedTouches[0].screenY - touchStartY;

  if (deltaY > 80 && Math.abs(deltaY) > Math.abs(deltaX)) {
    // Swipe down
    nowPlayingOverlay.classList.remove('active');
  } else if (deltaX < -50) {
    // Swipe left -> next
    skipNext();
  } else if (deltaX > 50) {
    // Swipe right -> prev
    skipPrev();
  }
}, { passive: true });

// Mouse drag simulation for desktop testing
let isMouseDown = false;
let mouseStartX = 0;
let mouseStartY = 0;

heroArtworkWrap.addEventListener('mousedown', (e) => {
  isMouseDown = true;
  mouseStartX = e.clientX;
  mouseStartY = e.clientY;
});

window.addEventListener('mouseup', (e) => {
  if (!isMouseDown) return;
  isMouseDown = false;
  const deltaX = e.clientX - mouseStartX;
  const deltaY = e.clientY - mouseStartY;

  if (deltaY > 80 && Math.abs(deltaY) > Math.abs(deltaX)) {
    nowPlayingOverlay.classList.remove('active');
  } else if (deltaX < -50) {
    skipNext();
  } else if (deltaX > 50) {
    skipPrev();
  }
});

// Playback Control Button Bindings
document.getElementById('miniPlayBtn').addEventListener('click', (e) => {
  e.stopPropagation();
  togglePlayPause();
});

document.getElementById('miniNextBtn').addEventListener('click', (e) => {
  e.stopPropagation();
  skipNext();
});

document.getElementById('npHeroPlayBtn').addEventListener('click', togglePlayPause);
document.getElementById('npNextBtn').addEventListener('click', skipNext);
document.getElementById('npPrevBtn').addEventListener('click', skipPrev);

document.getElementById('npShuffleBtn').addEventListener('click', () => {
  isShuffle = !isShuffle;
  updateUI();
});

document.getElementById('npRepeatBtn').addEventListener('click', () => {
  repeatMode = (repeatMode + 1) % 3;
  updateUI();
});

npSeekSlider.addEventListener('input', (e) => {
  seekTo(e.target.value / 100);
});

npFavBtn.addEventListener('click', () => {
  const track = currentQueue[currentTrackIndex];
  if (track) {
    track.isFavorite = !track.isFavorite;
    updateUI();
  }
});

// Favorites banner on home
document.getElementById('homeFavoritesBanner').addEventListener('click', () => {
  const favs = librarySongs.filter(s => s.isFavorite);
  if (favs.length > 0) playTrack(0, favs);
});

// Up Next Queue Drawer
const queueModal = document.getElementById('queueModal');
document.getElementById('btnOpenQueue').addEventListener('click', () => {
  renderQueueList();
  queueModal.classList.add('open');
});

document.getElementById('btnCloseQueue').addEventListener('click', () => {
  queueModal.classList.remove('open');
});

function renderQueueList() {
  document.getElementById('queueTitleHeader').textContent = `Up Next (${currentQueue.length})`;
  const container = document.getElementById('queueListContainer');
  container.innerHTML = currentQueue.map((track, idx) => `
    <div class="song-list-item ${idx === currentTrackIndex ? 'is-active' : ''}" onclick="playTrack(${idx})">
      <div class="song-thumb"><img src="${track.cover}" alt=""></div>
      <div class="song-info-col">
        <div class="song-item-title">${track.title}</div>
        <div class="song-item-meta">${track.artist}</div>
      </div>
    </div>
  `).join('');
}

// Equalizer Modal & Tuning
const eqModal = document.getElementById('eqModal');
document.getElementById('npEqBtn').addEventListener('click', () => eqModal.classList.add('open'));
document.getElementById('btnOpenEqualizer').addEventListener('click', () => eqModal.classList.add('open'));
document.getElementById('btnCloseEq').addEventListener('click', () => eqModal.classList.remove('open'));
document.getElementById('btnDoneEq').addEventListener('click', () => eqModal.classList.remove('open'));

document.getElementById('eqBassSlider').addEventListener('input', (e) => {
  initAudioContext();
  const val = parseFloat(e.target.value);
  document.getElementById('eqBassVal').textContent = (val > 0 ? `+${val}` : val) + " dB";
  if (bassFilter) bassFilter.gain.value = val;
});

document.getElementById('eqMidSlider').addEventListener('input', (e) => {
  initAudioContext();
  const val = parseFloat(e.target.value);
  document.getElementById('eqMidVal').textContent = (val > 0 ? `+${val}` : val) + " dB";
  if (midFilter) midFilter.gain.value = val;
});

document.getElementById('eqTrebSlider').addEventListener('input', (e) => {
  initAudioContext();
  const val = parseFloat(e.target.value);
  document.getElementById('eqTrebVal').textContent = (val > 0 ? `+${val}` : val) + " dB";
  if (trebFilter) trebFilter.gain.value = val;
});

// Theme Switching
const themeButtons = document.querySelectorAll('.theme-pill-btn');
themeButtons.forEach(btn => {
  btn.addEventListener('click', () => {
    themeButtons.forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    const mode = btn.getAttribute('data-theme-val');
    document.body.setAttribute('data-theme', mode);
  });
});

// Import Real Audio Files from Local Computer
const fileInput = document.getElementById('localFileInput');
document.getElementById('btnImportLocalFiles').addEventListener('click', () => fileInput.click());
document.getElementById('btnRescanMedia').addEventListener('click', () => fileInput.click());

fileInput.addEventListener('change', (e) => {
  const files = Array.from(e.target.files);
  if (files.length === 0) return;

  files.forEach((file, index) => {
    const url = URL.createObjectURL(file);
    const fileName = file.name.replace(/\.[^/.]+$/, "");
    librarySongs.unshift({
      id: Date.now() + index,
      title: fileName,
      artist: "Local Audio File",
      album: "Imported Media",
      durationMs: 240000,
      cover: "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500&q=80",
      audioUrl: url,
      isFavorite: false
    });
  });

  renderHomeScreen();
  renderLibrarySongs();
  playTrack(0, librarySongs);
  alert(`Added ${files.length} audio file(s) to Talo Music!`);
});

// Simulated Clock Time Update
function updateClock() {
  const now = new Date();
  const h = String(now.getHours()).padStart(2, '0');
  const m = String(now.getMinutes()).padStart(2, '0');
  document.getElementById('statusTime').textContent = `${h}:${m}`;
}
setInterval(updateClock, 1000);
updateClock();

// Boot App
renderHomeScreen();
// Pre-load first track into mini player ready to play
currentTrackIndex = 0;
updateUI();

// ==========================================================================
// MUSIC LAB CONTROLLER & MUSIC THEORY ENGINE
// ==========================================================================
const PITCHES = ['C', 'C#', 'D', 'Eb', 'E', 'F', 'F#', 'G', 'Ab', 'A', 'Bb', 'B'];
const QUALITIES = [
  { id: 'MAJOR', name: 'Maj', symbol: '' },
  { id: 'MINOR', name: 'm', symbol: 'm' },
  { id: 'DOMINANT_7', name: '7', symbol: '7' },
  { id: 'MAJOR_7', name: 'maj7', symbol: 'maj7' },
  { id: 'MINOR_7', name: 'm7', symbol: 'm7' },
  { id: 'SUS_4', name: 'sus4', symbol: 'sus4' },
  { id: 'DIMINISHED', name: 'dim', symbol: 'dim' },
  { id: 'AUGMENTED', name: 'aug', symbol: 'aug' }
];

window.labLoopStart = 0;
window.labLoopEnd = 16;
window.labLoopEnabled = false;
window.labTranspose = 0;
window.labNotation = 'chords';
let currentLabSong = null;
let currentLabData = null;
let activeEditingChordIndex = -1;

function getLabStorageKey(songId) {
  return `talo_lab_data_${songId}`;
}

function loadSongLabData(song) {
  const saved = localStorage.getItem(getLabStorageKey(song.id));
  if (saved) {
    try { return JSON.parse(saved); } catch (e) {}
  }

  // Generate initial analysis data
  const titleHash = Math.abs(song.title.split('').reduce((a, c) => (a << 5) - a + c.charCodeAt(0), 0));
  const tonic = titleHash % 12;
  const isMinor = (titleHash % 3) === 0;
  const bpm = 90 + (titleHash % 45);

  const durationSec = song.durationMs / 1000;
  const chordStepSec = 8;
  const chords = [];
  const progressionIntervals = isMinor ? [0, 3, 7, 5] : [0, 9, 5, 7];

  let cTime = 0;
  let cIdx = 0;
  while (cTime < durationSec) {
    const end = Math.min(durationSec, cTime + chordStepSec);
    const interval = progressionIntervals[cIdx % progressionIntervals.length];
    const root = (tonic + interval) % 12;
    const quality = (interval === 9 || (isMinor && interval === 0)) ? 'MINOR' : 'MAJOR';
    chords.push({
      id: cIdx + 1,
      root: root,
      quality: quality,
      startSec: cTime,
      endSec: end,
      source: 'Auto'
    });
    cTime = end;
    cIdx++;
  }

  const sections = [
    { name: "Intro", startSec: 0, endSec: Math.floor(durationSec * 0.12) },
    { name: "Verse 1", startSec: Math.floor(durationSec * 0.12), endSec: Math.floor(durationSec * 0.35) },
    { name: "Chorus", startSec: Math.floor(durationSec * 0.35), endSec: Math.floor(durationSec * 0.55) },
    { name: "Verse 2", startSec: Math.floor(durationSec * 0.55), endSec: Math.floor(durationSec * 0.75) },
    { name: "Bridge", startSec: Math.floor(durationSec * 0.75), endSec: Math.floor(durationSec * 0.88) },
    { name: "Outro", startSec: Math.floor(durationSec * 0.88), endSec: durationSec }
  ];

  const data = {
    tonic: tonic,
    mode: isMinor ? 'MINOR' : 'MAJOR',
    bpm: bpm,
    chords: chords,
    sections: sections
  };

  localStorage.setItem(getLabStorageKey(song.id), JSON.stringify(data));
  return data;
}

function saveCurrentLabData() {
  if (currentLabSong && currentLabData) {
    localStorage.setItem(getLabStorageKey(currentLabSong.id), JSON.stringify(currentLabData));
  }
}

// Open Music Lab Workspace
window.openMusicLab = function(songId) {
  const song = librarySongs.find(s => s.id === songId) || librarySongs[currentTrackIndex];
  if (!song) return;

  currentLabSong = song;
  currentLabData = loadSongLabData(song);
  window.labTranspose = 0;
  window.labNotation = 'chords';
  window.labLoopEnabled = false;

  document.getElementById('musicLabOverlay').classList.add('active');
  document.getElementById('labSongTitle').textContent = song.title;
  document.getElementById('labThumbImg').src = song.cover;

  // Sync playback with this track if not already playing it
  if (currentQueue[currentTrackIndex]?.id !== song.id) {
    playTrackById(song.id);
  }

  renderLabUI();
};

document.getElementById('btnDismissMusicLab').addEventListener('click', () => {
  document.getElementById('musicLabOverlay').classList.remove('active');
});

document.getElementById('btnNowPlayingOpenLab').addEventListener('click', () => {
  const curTrack = currentQueue[currentTrackIndex];
  if (curTrack) {
    window.openMusicLab(curTrack.id);
  }
});

// Render UI Components inside Music Lab
function renderLabUI() {
  if (!currentLabData) return;

  // Key & BPM Summary
  const effTonic = (currentLabData.tonic + window.labTranspose + 12) % 12;
  const tonicName = PITCHES[effTonic];
  const modeName = currentLabData.mode === 'MAJOR' ? 'Major' : 'Minor';
  document.getElementById('labKeyVal').textContent = `${tonicName} ${modeName}`;
  document.getElementById('labBpmVal').textContent = `${currentLabData.bpm} BPM`;

  // Transpose text
  const tStr = window.labTranspose === 0 ? '0 st' : (window.labTranspose > 0 ? `+${window.labTranspose} st` : `${window.labTranspose} st`);
  document.getElementById('labTransposeVal').textContent = tStr;

  // Sections
  const secRow = document.getElementById('labSectionsRow');
  secRow.innerHTML = currentLabData.sections.map(sec => {
    const isCur = currentPositionSec >= sec.startSec && currentPositionSec < sec.endSec;
    return `
      <div class="lab-section-tag ${isCur ? 'active' : ''}" onclick="seekTo(${sec.startSec / (currentLabSong.durationMs / 1000)})">
        ${sec.name}
      </div>
    `;
  }).join('');

  // Chords
  const chordRow = document.getElementById('labChordTimeline');
  chordRow.innerHTML = currentLabData.chords.map((chord, idx) => {
    const isCur = currentPositionSec >= chord.startSec && currentPositionSec < chord.endSec;
    const transRoot = (chord.root + window.labTranspose + 12) % 12;
    const chordText = formatChordNotation(transRoot, chord.quality, effTonic);

    return `
      <div class="chord-event-card ${isCur ? 'is-active' : ''}" onclick="openChordEditor(${idx})">
        <div class="chord-card-name">${chordText}</div>
        <div class="chord-card-footer">
          <span>${formatTime(chord.startSec)}</span>
          <span style="color: ${chord.source === 'User' ? 'var(--primary)' : 'var(--text-tertiary)'}; font-weight: 600;">${chord.source === 'User' ? '✓' : 'Auto'}</span>
        </div>
      </div>
    `;
  }).join('');

  // Looper Display
  document.getElementById('btnLoopSetA').textContent = `Set A (${formatTime(window.labLoopStart)})`;
  document.getElementById('btnLoopSetB').textContent = `Set B (${formatTime(window.labLoopEnd)})`;
  document.getElementById('labLoopStateText').textContent = window.labLoopEnabled ? 'Looping' : 'Loop Off';
  document.getElementById('btnLoopToggle').classList.toggle('active', window.labLoopEnabled);

  // Bottom Player bar
  const durationSec = currentLabSong.durationMs / 1000;
  document.getElementById('labSeekSlider').value = (currentPositionSec / durationSec) * 100;
  document.getElementById('labTimeElapsed').textContent = formatTime(currentPositionSec);
  document.getElementById('labTimeTotal').textContent = formatTime(durationSec);

  const playPath = '<path d="M8 5v14l11-7z"/>';
  const pausePath = '<path d="M6 19h4V5H6v14zm8-14v14h4V5h-4z"/>';
  document.getElementById('labPlayIcon').innerHTML = isPlaying ? pausePath : playPath;
}

window.updateLabUI = function() {
  if (document.getElementById('musicLabOverlay').classList.contains('active')) {
    renderLabUI();
  }
};

// Music Theory Notation Formatter
function formatChordNotation(root, qualityId, tonic) {
  const rootName = PITCHES[root];
  const qObj = QUALITIES.find(q => q.id === qualityId) || QUALITIES[0];

  if (window.labNotation === 'roman') {
    const interval = (root - tonic + 12) % 12;
    const romanMap = ['I', 'bII', 'II', 'bIII', 'III', 'IV', 'bV', 'V', 'bVI', 'VI', 'bVII', 'VII'];
    let num = romanMap[interval] || '?';
    if (qObj.symbol === 'm') num = num.toLowerCase();
    const suffix = qObj.symbol === '7' ? '7' : (qObj.symbol === 'maj7' ? 'maj7' : (qObj.symbol === 'dim' ? '°' : ''));
    return num + suffix;
  }

  if (window.labNotation === 'nashville') {
    const interval = (root - tonic + 12) % 12;
    const nashMap = ['1', 'b2', '2', 'b3', '3', '4', 'b5', '5', 'b6', '6', 'b7', '7'];
    const num = nashMap[interval] || '?';
    const suffix = qObj.symbol === 'm' ? 'm' : (qObj.symbol === '7' ? '7' : '');
    return num + suffix;
  }

  // Standard Chords
  return rootName + qObj.symbol;
}

// Transpose buttons
document.getElementById('btnTransposeDown').addEventListener('click', () => {
  window.labTranspose--;
  renderLabUI();
});
document.getElementById('btnTransposeUp').addEventListener('click', () => {
  window.labTranspose++;
  renderLabUI();
});

// Notation pills
document.querySelectorAll('.notation-btn[data-notation]').forEach(btn => {
  btn.addEventListener('click', () => {
    document.querySelectorAll('.notation-btn[data-notation]').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    window.labNotation = btn.getAttribute('data-notation');
    renderLabUI();
  });
});

// Loop Practice Buttons
document.getElementById('btnLoopSetA').addEventListener('click', () => {
  window.labLoopStart = currentPositionSec;
  if (window.labLoopEnd <= window.labLoopStart) {
    window.labLoopEnd = window.labLoopStart + 8;
  }
  renderLabUI();
});

document.getElementById('btnLoopSetB').addEventListener('click', () => {
  window.labLoopEnd = currentPositionSec;
  if (window.labLoopEnd <= window.labLoopStart) {
    window.labLoopStart = Math.max(0, window.labLoopEnd - 8);
  }
  renderLabUI();
});

document.getElementById('btnLoopToggle').addEventListener('click', () => {
  window.labLoopEnabled = !window.labLoopEnabled;
  if (window.labLoopEnabled) {
    currentPositionSec = window.labLoopStart;
    if (isPlayingUploadedFile) htmlAudio.currentTime = window.labLoopStart;
  }
  renderLabUI();
});

// Playback Speed Selector
document.querySelectorAll('.speed-btn[data-speed]').forEach(btn => {
  btn.addEventListener('click', () => {
    document.querySelectorAll('.speed-btn').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    const speed = parseFloat(btn.getAttribute('data-speed'));
    htmlAudio.playbackRate = speed;
  });
});

// Lab Bottom Player Controls
document.getElementById('labPlayBtn').addEventListener('click', togglePlayPause);
document.getElementById('labSeekSlider').addEventListener('input', (e) => {
  seekTo(e.target.value / 100);
});

// ==========================================================================
// CHORD EDITOR MODAL
// ==========================================================================
let editingRoot = 0;
let editingQuality = 'MAJOR';

window.openChordEditor = function(index) {
  activeEditingChordIndex = index;
  const chord = currentLabData.chords[index];
  editingRoot = chord.root;
  editingQuality = chord.quality;

  document.getElementById('chordEditorTitle').textContent = `Edit Chord (${formatChordNotation(editingRoot, editingQuality, currentLabData.tonic)})`;
  renderChordEditorGrids();
  document.getElementById('btnDeleteChord').style.display = 'block';
  document.getElementById('chordEditorModal').classList.add('open');
};

document.getElementById('btnLabAddChord').addEventListener('click', () => {
  activeEditingChordIndex = -1;
  editingRoot = currentLabData.tonic;
  editingQuality = 'MAJOR';

  document.getElementById('chordEditorTitle').textContent = 'Add Chord';
  renderChordEditorGrids();
  document.getElementById('btnDeleteChord').style.display = 'none';
  document.getElementById('chordEditorModal').classList.add('open');
});

function renderChordEditorGrids() {
  const pGrid = document.getElementById('chordEditorPitchGrid');
  pGrid.innerHTML = PITCHES.map((p, idx) => `
    <button class="notation-btn ${idx === editingRoot ? 'active' : ''}" onclick="setEditPitch(${idx})" style="padding: 10px 4px;">
      ${p}
    </button>
  `).join('');

  const qGrid = document.getElementById('chordEditorQualityGrid');
  qGrid.innerHTML = QUALITIES.map(q => `
    <button class="notation-btn ${q.id === editingQuality ? 'active' : ''}" onclick="setEditQuality('${q.id}')" style="padding: 10px 4px;">
      ${q.name}
    </button>
  `).join('');
}

window.setEditPitch = function(idx) {
  editingRoot = idx;
  renderChordEditorGrids();
  document.getElementById('chordEditorTitle').textContent = `Chord: ${PITCHES[editingRoot]}${QUALITIES.find(q=>q.id===editingQuality).symbol}`;
};

window.setEditQuality = function(qId) {
  editingQuality = qId;
  renderChordEditorGrids();
  document.getElementById('chordEditorTitle').textContent = `Chord: ${PITCHES[editingRoot]}${QUALITIES.find(q=>q.id===editingQuality).symbol}`;
};

document.getElementById('btnCloseChordEditor').addEventListener('click', () => {
  document.getElementById('chordEditorModal').classList.remove('open');
});

document.getElementById('btnSaveChord').addEventListener('click', () => {
  if (activeEditingChordIndex >= 0) {
    currentLabData.chords[activeEditingChordIndex].root = editingRoot;
    currentLabData.chords[activeEditingChordIndex].quality = editingQuality;
    currentLabData.chords[activeEditingChordIndex].source = 'User';
  } else {
    // Add new chord at current playback position
    currentLabData.chords.push({
      id: Date.now(),
      root: editingRoot,
      quality: editingQuality,
      startSec: currentPositionSec,
      endSec: currentPositionSec + 6,
      source: 'User'
    });
    currentLabData.chords.sort((a, b) => a.startSec - b.startSec);
  }
  saveCurrentLabData();
  document.getElementById('chordEditorModal').classList.remove('open');
  renderLabUI();
});

document.getElementById('btnDeleteChord').addEventListener('click', () => {
  if (activeEditingChordIndex >= 0) {
    currentLabData.chords.splice(activeEditingChordIndex, 1);
    saveCurrentLabData();
  }
  document.getElementById('chordEditorModal').classList.remove('open');
  renderLabUI();
});

// ==========================================================================
// KEY & BPM EDITOR MODAL
// ==========================================================================
document.getElementById('btnEditKeyBpm').addEventListener('click', () => {
  renderKeyBpmModal();
  document.getElementById('keyBpmEditorModal').classList.add('open');
});

function renderKeyBpmModal() {
  const grid = document.getElementById('keyTonicGrid');
  grid.innerHTML = PITCHES.map((p, idx) => `
    <button class="notation-btn ${idx === currentLabData.tonic ? 'active' : ''}" onclick="setKeyTonic(${idx})" style="padding: 8px 4px;">
      ${p}
    </button>
  `).join('');

  document.getElementById('btnModeMajor').classList.toggle('active', currentLabData.mode === 'MAJOR');
  document.getElementById('btnModeMinor').classList.toggle('active', currentLabData.mode === 'MINOR');
  document.getElementById('keyBpmSlider').value = currentLabData.bpm;
  document.getElementById('keyBpmDisplay').textContent = `${currentLabData.bpm} BPM`;
}

window.setKeyTonic = function(idx) {
  currentLabData.tonic = idx;
  renderKeyBpmModal();
};

document.getElementById('btnModeMajor').addEventListener('click', () => {
  currentLabData.mode = 'MAJOR';
  renderKeyBpmModal();
});

document.getElementById('btnModeMinor').addEventListener('click', () => {
  currentLabData.mode = 'MINOR';
  renderKeyBpmModal();
});

document.getElementById('keyBpmSlider').addEventListener('input', (e) => {
  currentLabData.bpm = parseInt(e.target.value);
  document.getElementById('keyBpmDisplay').textContent = `${currentLabData.bpm} BPM`;
});

document.getElementById('btnCloseKeyBpm').addEventListener('click', () => {
  document.getElementById('keyBpmEditorModal').classList.remove('open');
});

document.getElementById('btnSaveKeyBpm').addEventListener('click', () => {
  saveCurrentLabData();
  document.getElementById('keyBpmEditorModal').classList.remove('open');
  renderLabUI();
});

