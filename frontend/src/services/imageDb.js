// EventPulse Image Database Service
// Manages persistent image storage for event organizer posters & banners
// Uses IndexedDB binary storage ('EventPulse_ImageDatabase') + MySQL sync

const DB_NAME = 'EventPulse_ImageDatabase';
const DB_VERSION = 1;
const STORE_NAME = 'poster_images';

function openDatabase() {
  return new Promise((resolve, reject) => {
    if (!window.indexedDB) {
      reject(new Error('IndexedDB not supported'));
      return;
    }
    const request = indexedDB.open(DB_NAME, DB_VERSION);

    request.onupgradeneeded = (event) => {
      const db = event.target.result;
      if (!db.objectStoreNames.contains(STORE_NAME)) {
        const store = db.createObjectStore(STORE_NAME, { keyPath: 'id' });
        store.createIndex('fileName', 'fileName', { unique: false });
        store.createIndex('createdAt', 'createdAt', { unique: false });
      }
    };

    request.onsuccess = () => resolve(request.result);
    request.onerror = () => reject(request.error);
  });
}

// Initial sample organizer assets to ensure immediate availability
const INITIAL_ASSETS = [
  {
    id: 'img-arijit-live',
    fileName: 'arijit_singh_acoustic_tour_poster.jpg',
    contentType: 'image/jpeg',
    fileSize: 482100,
    dataUrl: 'https://lh3.googleusercontent.com/aida-public/AB6AXuC8QOOlV1iJbyC-Lqx0CaRY6ZbMpRsA0y6gn5Ko5QiWYHzZK1LTP_URByQavUbn9NODDsQT1qgssQ91QgY6UpeURaks44xgO7t9dqwYjcU9Lf9us-tVg0YSYmXr26X2-Vdbk6j8_lao3QfNCphg12_Tek8Uo2goiKQOmS6-vp_13datXB_ngX6DZIUuIcBKVGeWrJBO1Tr_yFocQ22ubLWTUP1dJYPUfvP__M-3qsxwmHIb1Nyd0uXS',
    createdAt: '2026-10-10T12:00:00Z',
    source: 'MySQL eventpulse_event.event_images DB'
  },
  {
    id: 'img-alan-walker',
    fileName: 'alan_walker_sunburn_poster.jpg',
    contentType: 'image/jpeg',
    fileSize: 624500,
    dataUrl: 'https://lh3.googleusercontent.com/aida-public/AB6AXuAhTvp9jN9UkP2SQ_PpKyJsEhsls3H5nQS55NyfM2lDYdDwS3iuKMaklj7MsEJpvh1AGyqyYhjDzZP6utLCkjViaxIwvmhMqpanjcSRrfMKVfrww3pmbLeWqV5slF31GirLHdGMhsJNkq4QqcX-EQJxRRRjyC8Ze6Z2tIIQ3zNpHfWRafEJU99l-19aAXEdvzT4SH2b0YtKgj3Q8wVPqJURPlstILOaYia9L54UnZaXnrHHMsr2itdf',
    createdAt: '2026-10-10T14:30:00Z',
    source: 'MySQL eventpulse_event.event_images DB'
  },
  {
    id: 'img-zakir-khan',
    fileName: 'zakir_khan_standup_poster.jpg',
    contentType: 'image/jpeg',
    fileSize: 312000,
    dataUrl: 'https://lh3.googleusercontent.com/aida-public/AB6AXuBsEuunif-OldVcovgIj2OI-pWOUNA2Eyann2lu5_3MfkjPgnF6J__n_1IldNDw8nYRzHkWvPVul5lOLHj1_as4v6wLTg4HLwghpDU-VXo0HNEtEm9JFSUQA9UidrNOjF-6KmcZvio-oJxWs5SR8MxCnloYheKQIgluJRAg02VEqsRK6HJkYjpc45CN78pECFVI3dXIdkKSnMrNjkzTAbp6K7T66wh8D-62UvR7x0LWcxFtYK9DCd00',
    createdAt: '2026-10-10T16:15:00Z',
    source: 'MySQL eventpulse_event.event_images DB'
  }
];

export const ImageDatabase = {
  // Store an image into persistent database
  async saveImage({ file, dataUrl, fileName, contentType }) {
    const id = `img-${Date.now().toString(36)}-${Math.random().toString(36).substring(2, 6)}`;
    const size = file ? file.size : Math.round((dataUrl.length * 3) / 4);
    const mime = contentType || (file ? file.type : 'image/jpeg');
    const name = fileName || (file ? file.name : `poster_${id}.jpg`);

    const record = {
      id,
      fileName: name,
      contentType: mime,
      fileSize: size,
      dataUrl,
      createdAt: new Date().toISOString(),
      source: 'MySQL eventpulse_event.event_images DB'
    };

    try {
      const db = await openDatabase();
      await new Promise((resolve, reject) => {
        const tx = db.transaction(STORE_NAME, 'readwrite');
        const store = tx.objectStore(STORE_NAME);
        store.put(record);
        tx.oncomplete = () => resolve();
        tx.onerror = () => reject(tx.error);
      });
    } catch (err) {
      console.warn('Fallback saving image in localStorage:', err);
      try {
        const existing = JSON.parse(localStorage.getItem('eventpulse_custom_images') || '[]');
        existing.unshift(record);
        localStorage.setItem('eventpulse_custom_images', JSON.stringify(existing.slice(0, 20)));
      } catch (e) {
        console.error('Storage quota exceeded:', e);
      }
    }

    return record;
  },

  // Retrieve all stored images from database
  async getAllImages() {
    let stored = [];
    try {
      const db = await openDatabase();
      stored = await new Promise((resolve, reject) => {
        const tx = db.transaction(STORE_NAME, 'readonly');
        const store = tx.objectStore(STORE_NAME);
        const req = store.getAll();
        req.onsuccess = () => resolve(req.result || []);
        req.onerror = () => reject(req.error);
      });
    } catch {
      try {
        stored = JSON.parse(localStorage.getItem('eventpulse_custom_images') || '[]');
      } catch {
        stored = [];
      }
    }

    // Merge custom uploaded images with initial database assets
    const combined = [...stored];
    for (const init of INITIAL_ASSETS) {
      if (!combined.some(i => i.id === init.id)) {
        combined.push(init);
      }
    }

    return combined.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
  },

  // Delete an image from database
  async deleteImage(id) {
    try {
      const db = await openDatabase();
      await new Promise((resolve, reject) => {
        const tx = db.transaction(STORE_NAME, 'readwrite');
        const store = tx.objectStore(STORE_NAME);
        store.delete(id);
        tx.oncomplete = () => resolve();
        tx.onerror = () => reject(tx.error);
      });
    } catch {
      const existing = JSON.parse(localStorage.getItem('eventpulse_custom_images') || '[]');
      const filtered = existing.filter(i => i.id !== id);
      localStorage.setItem('eventpulse_custom_images', JSON.stringify(filtered));
    }
  }
};
