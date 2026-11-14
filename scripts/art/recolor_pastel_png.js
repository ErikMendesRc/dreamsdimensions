#!/usr/bin/env node
/**
 * DreamsDimensions: pixel-preserving pastel palette pass for the 17 dream-only
 * 16x16 terrain/vegetation sprites. Node 24, no external packages.
 * Run from repository root: node scripts/art/recolor_pastel_png.js
 * Do not include Overworld ores/marked stone in this operation.
 */
globalThis.DreamPastelPNG = (() => {
"use strict";
const specs=[
 ["blue_dream_stone",218,.30,.65,"campo"],
 ["blue_dream_cobblestone",222,.32,.59,"campo"],
 ["blue_dream_dirt",237,.25,.66,"campo"],
 ["blue_dream_grass_top",186,.36,.72,"campo"],
 ["blue_dream_grass_side",236,.25,.66,"campo"],
 ["serene_stone",271,.27,.70,"dreamscape"],
 ["dream_dirt_block",312,.25,.66,"dreamscape"],
 ["dream_grass_block_top",147,.28,.72,"dreamscape"],
 ["dream_grass_block_side",312,.25,.66,"dreamscape"],
 ["dream_sand_block",39,.32,.79,"shared"],
 ["dream_infused_stone",262,.32,.68,"shared"],
 ["dream_shimmer_block",280,.31,.73,"shared"],
 ["dream_leaves_block",156,.30,.70,"shared"],
 ["dream_log_block",319,.23,.60,"shared"],
 ["dream_log_block_top",319,.22,.65,"shared"],
 ["dream_glow_moss",169,.35,.70,"shared"],
 ["dream_flower_block",313,.39,.73,"shared"]
];
const clamp=(v,a,b)=>Math.max(a,Math.min(b,v));
function decode64(s){const t="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";let o=[],bits=0,n=0;for(const c of s){if(c==="=")break;let v=t.indexOf(c);if(v<0)continue;bits=(bits<<6)|v;n+=6;if(n>=8){n-=8;o.push((bits>>>n)&255);bits&=(1<<n)-1;}}return Uint8Array.from(o);}
function encode64(a){const t="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";let o="";for(let i=0;i<a.length;i+=3){let v=(a[i]<<16)|((a[i+1]||0)<<8)|(a[i+2]||0);o+=t[(v>>>18)&63]+t[(v>>>12)&63]+(i+1<a.length?t[(v>>>6)&63]:"=")+(i+2<a.length?t[v&63]:"=");}return o;}
const read32=(u,o)=>(u[o]*16777216+(u[o+1]<<16)+(u[o+2]<<8)+u[o+3])>>>0;
const be32=v=>[(v>>>24)&255,(v>>>16)&255,(v>>>8)&255,v&255];
const ct=Array.from({length:256},(_,i)=>{let c=i;for(let j=0;j<8;j++)c=c&1?0xedb88320^(c>>>1):c>>>1;return c>>>0;});
function crc32(u){let c=0xffffffff;for(const v of u)c=ct[(c^v)&255]^(c>>>8);return(c^0xffffffff)>>>0;}
function chunk(tag,data){let n=Array.from(tag).map(c=>c.charCodeAt(0)),b=Uint8Array.from([...n,...data]);return Uint8Array.from([...be32(data.length),...b,...be32(crc32(b))]);}
function huffman(lengths){let max=Math.max(...lengths),count=Array(max+1).fill(0),next=Array(max+1).fill(0),map=new Map(),code=0;for(const l of lengths)if(l)count[l]++;for(let i=1;i<=max;i++){code=(code+(count[i-1]||0))<<1;next[i]=code;}for(let i=0;i<lengths.length;i++){let l=lengths[i];if(l)map.set(l*65536+(next[l]++),i);}return {max,map};}
function inflateZlib(dat){
 if(dat.length<6||(dat[0]&15)!==8||((dat[0]<<8)+dat[1])%31!==0)throw Error("Invalid zlib");
 let pos=2,buf=0,left=0,out=[];function bits(n){if(!n)return 0;while(left<n){if(pos>=dat.length)throw Error("End of deflate");buf|=dat[pos++]<<left;left+=8;}let v=buf&((1<<n)-1);buf>>>=n;left-=n;return v;}
 function symbol(t){let code=0;for(let l=1;l<=t.max;l++){code=(code<<1)|bits(1);let v=t.map.get(l*65536+code);if(v!==undefined)return v;}throw Error("Huffman code");}
 const lb=[3,4,5,6,7,8,9,10,11,13,15,17,19,23,27,31,35,43,51,59,67,83,99,115,131,163,195,227,258];
 const le=[0,0,0,0,0,0,0,0,1,1,1,1,2,2,2,2,3,3,3,3,4,4,4,4,5,5,5,5,0];
 const db=[1,2,3,4,5,7,9,13,17,25,33,49,65,97,129,193,257,385,513,769,1025,1537,2049,3073,4097,6145,8193,12289,16385,24577];
 const de=[0,0,0,0,1,1,2,2,3,3,4,4,5,5,6,6,7,7,8,8,9,9,10,10,11,11,12,12,13,13];
 let done=false,blocks=0;
 while(!done){if(++blocks>1000)throw Error("Too many deflate blocks");done=!!bits(1);let type=bits(2);
 if(type===0){let rem=left%8;if(rem)bits(rem);let n=bits(16),inv=bits(16);if(((n^inv)&65535)!==65535)throw Error("Bad stored block");for(let j=0;j<n;j++)out.push(bits(8));continue;}
 let lt,dt;
 if(type===1){let l=Array(288);for(let i=0;i<288;i++)l[i]=i<144?8:i<256?9:i<280?7:8;lt=huffman(l);dt=huffman(Array(32).fill(5));}
 else if(type===2){let nl=bits(5)+257,nd=bits(5)+1,nc=bits(4)+4,order=[16,17,18,0,8,7,9,6,10,5,11,4,12,3,13,2,14,1,15],lens=Array(19).fill(0);for(let i=0;i<nc;i++)lens[order[i]]=bits(3);let t=huffman(lens),all=[];while(all.length<nl+nd){let s=symbol(t);if(s<16)all.push(s);else if(s===16){if(!all.length)throw Error("No previous length");let n=bits(2)+3,v=all[all.length-1];for(let j=0;j<n;j++)all.push(v);}else{let n=s===17?bits(3)+3:bits(7)+11;for(let j=0;j<n;j++)all.push(0);}}lt=huffman(all.slice(0,nl));dt=huffman(all.slice(nl,nl+nd));}
 else throw Error("Invalid deflate type");
 while(true){let s=symbol(lt);if(s<256){out.push(s);continue;}if(s===256)break;let k=s-257;if(k>=lb.length)throw Error("Invalid length");let n=lb[k]+bits(le[k]),d=symbol(dt);if(d>=db.length)throw Error("Invalid distance");let distance=db[d]+bits(de[d]);if(distance>out.length)throw Error("Backref");for(let j=0;j<n;j++)out.push(out[out.length-distance]);if(out.length>10000000)throw Error("Output too large");}
 }
 let result=Uint8Array.from(out),a=1,b=0;for(const v of result){a=(a+v)%65521;b=(b+a)%65521;}if(read32(dat,dat.length-4)!==(((b<<16)|a)>>>0))throw Error("Adler32");return result;
}
function decodePng(data){
 if(data.length<45||Array.from(data.slice(0,8)).join(",")!=="137,80,78,71,13,10,26,10")throw Error("PNG header");
 let off=8,w,h,depth,color,ids=[];
 while(off+12<=data.length){let n=read32(data,off),tag=String.fromCharCode(...data.slice(off+4,off+8)),p=data.slice(off+8,off+8+n);if(off+n+12>data.length)throw Error("PNG truncated");if(crc32(data.slice(off+4,off+8+n))!==read32(data,off+8+n))throw Error("CRC "+tag);off+=n+12;
 if(tag==="IHDR"){w=read32(p,0);h=read32(p,4);depth=p[8];color=p[9];if(p[12])throw Error("Interlaced PNG");}if(tag==="IDAT")ids.push(...p);if(tag==="IEND")break;}
 if(w!==16||h!==16||depth!==8||color!==6)throw Error("Expected RGBA 16x16, received "+[w,h,depth,color]);
 const raw=inflateZlib(Uint8Array.from(ids)),stride=w*4,out=new Uint8Array(w*h*4);
 if(raw.length!==h*(stride+1))throw Error("PNG scanline length");
 let pos=0;for(let y=0;y<h;y++){let f=raw[pos++];if(f>4)throw Error("PNG filter");for(let x=0;x<stride;x++){let v=raw[pos++],l=x>=4?out[y*stride+x-4]:0,u=y?out[(y-1)*stride+x]:0,c=y&&x>=4?out[(y-1)*stride+x-4]:0,p=0;if(f===1)p=l;if(f===2)p=u;if(f===3)p=(l+u)>>1;if(f===4){let t=l+u-c,al=Math.abs(t-l),au=Math.abs(t-u),ac=Math.abs(t-c);p=al<=au&&al<=ac?l:au<=ac?u:c;}out[y*stride+x]=(v+p)&255;}}
 return {width:w,height:h,pixels:out};
}
function encodePng(w,h,pix){
 let raw=[],stride=w*4;for(let y=0;y<h;y++){raw.push(0);for(let i=0;i<stride;i++)raw.push(pix[y*stride+i]);}
 let a=1,b=0;for(const v of raw){a=(a+v)%65521;b=(b+a)%65521;}let n=raw.length;
 let z=Uint8Array.from([0x78,0x01,1,n&255,(n>>>8)&255,(~n)&255,((~n)>>>8)&255,...raw,...be32(((b<<16)|a)>>>0)]);
 return Uint8Array.from([Uint8Array.from([137,80,78,71,13,10,26,10]),chunk("IHDR",Uint8Array.from([...be32(w),...be32(h),8,6,0,0,0])),chunk("IDAT",z),chunk("IEND",new Uint8Array())].flatMap(v=>Array.from(v)));
}
function rgb(h,s,l){h=((h%360)+360)%360;let c=(1-Math.abs(2*l-1))*s,x=c*(1-Math.abs((h/60)%2-1)),m=l-c/2,v=h<60?[c,x,0]:h<120?[x,c,0]:h<180?[0,c,x]:h<240?[0,x,c]:h<300?[x,0,c]:[c,0,x];return v.map(x=>Math.round(255*clamp(x+m,0,1)));}
function recolor(original,sp){
 let [name,h,s,l]=sp,p=original.pixels,out=new Uint8Array(p.length),sum=0,count=0;
 for(let i=0;i<p.length;i+=4)if(p[i+3]){sum+=(.2126*p[i]+.7152*p[i+1]+.0722*p[i+2])/255;count++;}
 let mean=sum/(count||1);
 for(let i=0;i<p.length;i+=4){let alpha=p[i+3];out[i+3]=alpha;if(!alpha)continue;let y=Math.floor(i/64),lum=(.2126*p[i]+.7152*p[i+1]+.0722*p[i+2])/255,hh=h,ss=s,ll=l+(lum-mean)*.93;
 if(name==="blue_dream_grass_side"&&y<4){hh=186;ss=.36;ll=.72+(lum-mean)*.78;}
 if(name==="dream_grass_block_side"&&y<4){hh=147;ss=.28;ll=.72+(lum-mean)*.78;}
 if(name==="dream_flower_block"&&p[i+1]>p[i]*1.12&&p[i+1]>p[i+2]*1.06){hh=154;ss=.35;ll=.64+(lum-mean)*.9;}
 let v=rgb(hh,ss,clamp(ll,name.includes("stone")?.40:.42,.88));out.set(v,i);}
 return {width:original.width,height:original.height,pixels:out};
}
function convert(bytes,sp){let input=decodePng(bytes),result=recolor(input,sp),converted=encodePng(result.width,result.height,result.pixels),check=decodePng(converted);
 if(check.pixels.some((x,i)=>x!==result.pixels[i]))throw Error("Output roundtrip mismatch: "+sp[0]);return converted;
}
return {specs,decode64,encode64,decodePng,encodePng,recolor,convert};
})();
if(typeof require==="function"&&typeof module!=="undefined"&&require.main===module){
 const fs=require("node:fs"),path=require("node:path"),child=require("node:child_process"),t=globalThis.DreamPastelPNG;
 // Read approved pre-pastel sprites from Git rather than repeatedly tinting already tinted pixels.
 // Deterministic and idempotent: --check is default; --apply explicitly restores the intended result.
 const baseline="e50969944bbe3fea6eb1c6da781b83738ee9ec57",apply=process.argv.includes("--apply");
 let drift=0;
 for(const spec of t.specs){
  const rel="src/main/resources/assets/dreamsdimensions/textures/block/"+spec[0]+".png";
  const f=path.join(process.cwd(),rel);
  const original=child.execFileSync("git",["show",baseline+":"+rel]);
  const expected=t.convert(Uint8Array.from(original),spec),current=fs.readFileSync(f);
  const same=current.length===expected.length&&current.every((v,i)=>v===expected[i]);
  if(same)process.stdout.write("PASS "+spec[0]+"\n");
  else{drift++;if(apply){fs.writeFileSync(f,expected);process.stdout.write("RESTORED "+spec[0]+"\n");}else process.stdout.write("DRIFT "+spec[0]+" (run with --apply to restore)\n");}
 }
 if(drift&&!apply)process.exitCode=1;
}
