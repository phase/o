from fbuild.builders.c import guess_static
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
        make_option('--use-color', help='Use colored output', action='store_true')
    ))

@fbuild.db.caches
def configure(ctx):
    c = guess_static(ctx, debug=ctx.options.buildtype == 'debug',
            optimize=ctx.options.buildtype == 'release',
            flags=['-fdiagnostics-color'] if ctx.options.use_color else [],
            platform_options=[
                ({'posix'}, {'external_libs': ['m']})
            ])
    return c

def build(ctx):
    c = configure(ctx)
    c.build_exe('o2', ['o2.c'])
    c.build_exe('tst', ['o2.c'], macros=['UTEST'])
