from fbuild.builders.c import guess_static, guess_shared
from fbuild.builders import find_program
from fbuild.record import Record
from fbuild.path import Path
import fbuild.db

from optparse import make_option
import sys

def pre_options(parser):
    group = parser.add_option_group('config options')
    group.add_options((
        make_option('--buildtype', help='The build type',
                    choices=['debug', 'release'], default='debug'),
        make_option('--cc', help='Use the given C compiler'),
        make_option('--cflag', help='Pass the given flag to the C compiler',
                    action='append', default=[]),
        make_option('--use-color', help='Use colored output', action='store_true')
    ))

@fbuild.db.caches
def configure(ctx):
    kw = dict(
        debug=ctx.options.buildtype == 'debug',
        optimize=ctx.options.buildtype == 'release',
        flags=['-fdiagnostics-color'] if ctx.options.use_color else [],
        platform_options=[
            ({'posix'}, {'external_libs': ['m']})
        ]
    )
    kw['flags'].extend(ctx.options.cflag)
    static = guess_static(ctx, **kw)
    shared = guess_shared(ctx, **kw)
    return Record(static=static, shared=shared)

def build(ctx):
    rec = configure(ctx)
    static = rec.static
    shared = rec.shared
    libregexp = static.build_lib('regexp9', Path.glob('libregexp/*.c',
                                                      exclude=['*test*.c']))
    static.build_exe('o', ['o.c'])
    static.build_exe('tst', ['o.c'], macros=['UTEST'])
