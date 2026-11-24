from pathlib import Path
import json,subprocess,zipfile,hashlib,shutil,time,concurrent.futures,datetime
P=Path('experiments/construction/011-video/implement-01');scratch=Path(json.loads((P/'entry.json').read_text())['scratch']);ffmpeg=json.loads((P/'tool.json').read_text())['binary'];out=Path('Projeto/src/main/resources/media')
def sha(path):
 h=hashlib.sha256()
 with path.open('rb') as f:
  for b in iter(lambda:f.read(1048576),b''):h.update(b)
 return h.hexdigest()
def prepare(alias):
 record=P/(alias+'-download.json');deadline=time.monotonic()+1250
 while not record.exists():
  if time.monotonic()>deadline:raise RuntimeError('Download metadata absent: '+alias)
  time.sleep(1)
 download=json.loads(record.read_text());assert download['exit_code']==0,download;source=Path(download['cache']);assert sha(source)==download['sha256']
 member=None
 if source.suffix=='.zip':
  with zipfile.ZipFile(source) as z:
   candidates=[i for i in z.infolist() if i.filename.lower().endswith(('.mp4','.mkv')) and not i.filename.startswith('__MACOSX/')];assert len(candidates)==1,[(i.filename,i.file_size) for i in candidates]
   info=candidates[0];assert 1<=info.file_size<=1100000000;member=info.filename;extracted=scratch/(alias+Path(member).suffix);assert not extracted.exists()
   with z.open(info) as inp,extracted.open('wb') as target:shutil.copyfileobj(inp,target,1048576)
   assert extracted.stat().st_size==info.file_size
  source=extracted
 frames=scratch/(alias+'-frames');frames.mkdir();target=out/(alias.lower()+'.Mjpeg');assert not target.exists();start=datetime.datetime.now(datetime.timezone.utc).isoformat()
 vf='fps=10,scale=640:360:force_original_aspect_ratio=decrease,pad=640:360:(ow-iw)/2:(oh-ih)/2,setsar=1'
 cmd=[ffmpeg,'-nostdin','-hide_banner','-loglevel','info','-i',str(source),'-map','0:v:0','-vf',vf,'-an','-sn','-dn','-c:v','mjpeg','-q:v','5','-pix_fmt','yuvj420p','-threads','1','-progress','pipe:1','-nostats','-f','image2',str(frames/'%08d.jpg')]
 with (P/(alias+'-ffmpeg-progress.log')).open('w') as stdout,(P/(alias+'-ffmpeg.log')).open('w') as stderr:r=subprocess.run(cmd,stdout=stdout,stderr=stderr,timeout=900)
 result={'alias':alias,'started_at':start,'finished_at':datetime.datetime.now(datetime.timezone.utc).isoformat(),'download':download,'extracted_member':member,'source_cache':str(source),'source_size':source.stat().st_size,'source_sha256':sha(source),'command':cmd,'exit_code':r.returncode,'preparation':{'full_video':True,'credits_not_trimmed':True,'fps_requested':10,'canvas':[640,360],'aspect_ratio_preserved':True,'SAR':'1:1','jpeg_q':5,'pixel_format':'yuvj420p','audio_in_profile':False}}
 (P/(alias+'-preparation.json')).write_text(json.dumps(result,indent=2)+'\n');assert r.returncode==0,result
 files=sorted(frames.glob('*.jpg'));assert files and len(files)<100000;assert [x.name for x in files]==[f'{i:08d}.jpg' for i in range(1,len(files)+1)]
 # Validate every JPEG length before writing the candidate; no truncation or cap adjustment.
 lengths=[f.stat().st_size for f in files];assert all(1<=x<=99999 for x in lengths),(alias,min(lengths),max(lengths))
 partial=target.with_suffix('.Mjpeg.part')
 with partial.open('wb') as packed:
  for f,L in zip(files,lengths):
   packed.write(f'{L:05d}'.encode('ascii'))
   with f.open('rb') as inp:shutil.copyfileobj(inp,packed,99999)
 partial.rename(target)
 result.update({'output':str(target),'output_sha256':sha(target),'output_bytes':target.stat().st_size,'frames':len(files),'min_JPEG':min(lengths),'max_JPEG':max(lengths),'framing':'5 ASCII digits then exact JPEG bytes,no delimiters','duration_at_requested_fps_seconds':len(files)/10,'playback_FPS_measured':None})
 (P/(alias+'-preparation.json')).write_text(json.dumps(result,indent=2)+'\n');print(alias,'PREPARED',len(files),min(lengths),max(lengths),result['output_sha256'],flush=True);return result
with concurrent.futures.ThreadPoolExecutor(max_workers=3) as pool:results=list(pool.map(prepare,['B','C','D']))
(P/'preparation-results.json').write_text(json.dumps(results,indent=2)+'\n')
